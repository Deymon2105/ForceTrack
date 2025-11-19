package com.example.forcetrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.UsuarioEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.network.repository.RemoteRepository
import com.example.forcetrack.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// sealed class para representar los diferentes estados de la autenticación
enum class AuthState {
    LOGGED_OUT, // El usuario no está logueado
    LOGGED_IN,  // El usuario ha iniciado sesión
    LOADING,    // Se está procesando una operación
    ERROR       // Ha ocurrido un error
}

data class AuthUiState(
    val authState: AuthState = AuthState.LOGGED_OUT,
    val currentUser: UsuarioEntity? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: ForceTrackRepository,
    private val sessionManager: com.example.forcetrack.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Repositorio remoto para conectarse a Xano
    private val remoteRepository = RemoteRepository()

    // ⛔ ANTI-SPAM: Control de operaciones en proceso
    private val _operacionesEnProceso = MutableStateFlow<Set<String>>(emptySet())

    private fun isOperacionEnProceso(operacion: String): Boolean {
        return _operacionesEnProceso.value.contains(operacion)
    }

    private fun marcarOperacionEnProceso(operacion: String) {
        _operacionesEnProceso.update { it + operacion }
    }

    private fun liberarOperacion(operacion: String) {
        _operacionesEnProceso.update { it - operacion }
    }

    init {
        // Intentar restaurar sesión desde DataStore al iniciar el ViewModel
        viewModelScope.launch {
            try {
                val savedId = sessionManager.getCurrentUserId()
                if (savedId != null) {
                    val user = repository.obtenerUsuarioPorId(savedId)
                    if (user != null) {
                        _uiState.update { AuthUiState(authState = AuthState.LOGGED_IN, currentUser = user) }
                    } else {
                        // Si no existe el usuario en DB, limpiar sesión guardada
                        sessionManager.clearCurrentUser()
                    }
                }
            } catch (_: Exception) {
                // ignore restore errors; dejar como LOGGED_OUT
            }
        }
    }

    fun login(correo: String, contrasena: String) {
        // ANTI-SPAM: Evitar múltiples clicks en login
        if (isOperacionEnProceso("login")) {
            Log.d("AuthViewModel", "Login ya en proceso, ignorando...")
            return
        }

        // Validar campos antes de proceder
        val validation = ValidationUtils.validateLogin(correo, contrasena)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    authState = AuthState.ERROR,
                    errorMessage = validation.emailError ?: validation.passwordError ?: "Datos inválidos"
                )
            }
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("login")
            _uiState.update { it.copy(authState = AuthState.LOADING) }
            try {
                Log.d("AuthViewModel", "Intentando login con Xano usando correo: $correo")

                // Intentar login con Xano primero
                remoteRepository.login(correo, contrasena)
                    .onSuccess { authResponse ->
                        Log.d("AuthViewModel", "Login exitoso en Xano")
                        authResponse.usuario?.let { usuarioDto ->
                            // Guardar/actualizar usuario en la base de datos local
                            val localUser = UsuarioEntity(
                                id = usuarioDto.id,
                                nombreUsuario = usuarioDto.nombreUsuario,
                                correo = usuarioDto.correo,
                                contrasena = contrasena
                            )

                            // Insertar o actualizar en la BD local
                            try {
                                repository.registrarUsuario(
                                    localUser.nombreUsuario,
                                    localUser.correo,
                                    localUser.contrasena
                                )
                            } catch (_: Exception) {
                                // Si ya existe, no pasa nada
                            }

                            // Guardar sesión
                            try {
                                sessionManager.saveCurrentUserId(usuarioDto.id)
                            } catch (_: Exception) { }

                            _uiState.update {
                                AuthUiState(authState = AuthState.LOGGED_IN, currentUser = localUser)
                            }
                            liberarOperacion("login")
                        } ?: run {
                            _uiState.update {
                                it.copy(
                                    authState = AuthState.ERROR,
                                    errorMessage = "Error obteniendo datos del usuario"
                                )
                            }
                            liberarOperacion("login")
                        }
                    }
                    .onFailure { error ->
                        Log.e("AuthViewModel", "Error en login Xano: ${error.message}")
                        // Intentar login local como fallback (buscar por correo)
                        try {
                            // Buscar usuario por correo en la BD local
                            val localUser = repository.iniciarSesionPorCorreo(correo, contrasena)
                            if (localUser != null) {
                                Log.d("AuthViewModel", "Login local exitoso (modo offline)")
                                try {
                                    sessionManager.saveCurrentUserId(localUser.id)
                                } catch (_: Exception) { }
                                _uiState.update {
                                    AuthUiState(authState = AuthState.LOGGED_IN, currentUser = localUser)
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        authState = AuthState.ERROR,
                                        errorMessage = "Correo o contraseña incorrectos"
                                    )
                                }
                            }
                        } catch (_: Exception) {
                            _uiState.update {
                                it.copy(
                                    authState = AuthState.ERROR,
                                    errorMessage = "Correo o contraseña incorrectos"
                                )
                            }
                        }
                        liberarOperacion("login")
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error general en login: ${e.message}")
                _uiState.update {
                    it.copy(authState = AuthState.ERROR, errorMessage = "Error: ${e.message}")
                }
                liberarOperacion("login")
            }
        }
    }

    fun register(nombreUsuario: String, correo: String, contrasena: String) {
        // ANTI-SPAM: Evitar múltiples clicks en registro
        if (isOperacionEnProceso("register")) {
            Log.d("AuthViewModel", "Registro ya en proceso, ignorando...")
            return
        }

        // Validar campos antes de proceder
        val validation = ValidationUtils.validateRegistration(nombreUsuario, correo, contrasena)
        if (!validation.isValid) {
            val errorMsg = validation.usernameError
                ?: validation.emailError
                ?: validation.passwordError
                ?: "Datos inválidos"
            _uiState.update {
                it.copy(authState = AuthState.ERROR, errorMessage = errorMsg)
            }
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("register")
            _uiState.update { it.copy(authState = AuthState.LOADING) }
            try {
                Log.d("AuthViewModel", "Intentando registro en Xano...")
                Log.d("AuthViewModel", "   Usuario: $nombreUsuario")
                Log.d("AuthViewModel", "   Correo: $correo")

                // Registrar en Xano primero
                remoteRepository.register(nombreUsuario, correo, contrasena)
                    .onSuccess { authResponse ->
                        Log.d("AuthViewModel", "Registro exitoso en Xano!")
                        authResponse.usuario?.let { usuarioDto ->
                            Log.d("AuthViewModel", "   ID generado: ${usuarioDto.id}")

                            // Guardar en la base de datos local
                            val localUser = UsuarioEntity(
                                id = usuarioDto.id,
                                nombreUsuario = usuarioDto.nombreUsuario,
                                correo = usuarioDto.correo,
                                contrasena = contrasena
                            )

                            // Insertar en BD local
                            try {
                                repository.registrarUsuario(
                                    localUser.nombreUsuario,
                                    localUser.correo,
                                    localUser.contrasena
                                )
                            } catch (_: Exception) {
                                Log.w("AuthViewModel", "Usuario ya existe localmente")
                            }

                            // Guardar sesión
                            try {
                                sessionManager.saveCurrentUserId(usuarioDto.id)
                            } catch (_: Exception) { }

                            _uiState.update {
                                AuthUiState(authState = AuthState.LOGGED_IN, currentUser = localUser)
                            }
                            liberarOperacion("register")
                        } ?: run {
                            Log.e("AuthViewModel", "No se recibieron datos del usuario")
                            _uiState.update {
                                it.copy(
                                    authState = AuthState.ERROR,
                                    errorMessage = "Error al crear el usuario"
                                )
                            }
                            liberarOperacion("register")
                        }
                    }
                    .onFailure { error ->
                        Log.e("AuthViewModel", "Error en registro Xano: ${error.message}")
                        error.printStackTrace()
                        _uiState.update {
                            it.copy(
                                authState = AuthState.ERROR,
                                errorMessage = "Error al conectar con el servidor: ${error.message}"
                            )
                        }
                        liberarOperacion("register")
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error general en registro: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(authState = AuthState.ERROR, errorMessage = "Error: ${e.message}")
                }
                liberarOperacion("register")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { sessionManager.clearCurrentUser() } catch (_: Exception) { /* ignore */ }
            _uiState.value = AuthUiState()
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, authState = AuthState.LOGGED_OUT) }
    }
}
