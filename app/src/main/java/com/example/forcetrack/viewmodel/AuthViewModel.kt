package com.example.forcetrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.UsuarioEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.network.RetrofitClient
import com.example.forcetrack.network.dto.LoginRequest
import com.example.forcetrack.network.dto.CreateUsuarioRequest
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
                Log.d("AuthViewModel", "Intentando login con backend usando correo: $correo")

                // Intentar login con backend
                val loginRequest = LoginRequest(correo = correo, contrasena = contrasena)
                val response = RetrofitClient.authApi.login(loginRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val usuarioDto = response.body()!!
                    Log.d("AuthViewModel", "Login exitoso en backend")
                    
                    // Validar que los campos necesarios no sean null
                    if (usuarioDto.nombreUsuario.isNullOrBlank() || usuarioDto.correo.isNullOrBlank()) {
                        Log.e("AuthViewModel", "Error: El usuario del backend tiene campos null")
                        _uiState.update {
                            it.copy(
                                authState = AuthState.ERROR,
                                errorMessage = "Error: Datos del usuario inválidos"
                            )
                        }
                        liberarOperacion("login")
                        return@launch
                    }
                    
                    // Guardar/actualizar usuario en la base de datos local
                    val localUser = UsuarioEntity(
                        id = usuarioDto.id,
                        nombreUsuario = usuarioDto.nombreUsuario,
                        correo = usuarioDto.correo,
                        contrasena = contrasena
                    )

                    // Insertar o actualizar en la BD local usando el mismo ID del backend
                    try {
                        repository.insertarUsuarioConId(
                            id = usuarioDto.id,
                            nombreUsuario = localUser.nombreUsuario,
                            correo = localUser.correo,
                            contrasena = localUser.contrasena
                        )
                    } catch (_: Exception) {
                        // Si ya existe o falla la inserción, continuar sin bloquear el login
                    }

                    // Guardar sesión
                    try {
                        sessionManager.saveCurrentUserId(usuarioDto.id)
                    } catch (_: Exception) { }

                    _uiState.update {
                        AuthUiState(authState = AuthState.LOGGED_IN, currentUser = localUser)
                    }
                } else {
                    // Manejar errores del servidor con mensajes claros
                    val errorMessage = obtenerMensajeError(response.code(), response.errorBody()?.string())
                    Log.e("AuthViewModel", "Error en login backend: ${response.code()} - $errorMessage")
                    
                    _uiState.update {
                        it.copy(
                            authState = AuthState.ERROR,
                            errorMessage = errorMessage
                        )
                    }
                }
                liberarOperacion("login")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error general en login: ${e.message}")
                e.printStackTrace()
                
                // Determinar mensaje de error amigable
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ->
                        "No se puede conectar al servidor. Verifica tu conexión a internet."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "La conexión tardó demasiado. Por favor, intenta de nuevo."
                    else ->
                        "Error al iniciar sesión. Por favor, verifica tu correo y contraseña."
                }
                
                _uiState.update {
                    it.copy(authState = AuthState.ERROR, errorMessage = errorMessage)
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
                Log.d("AuthViewModel", "Intentando registro en backend...")
                Log.d("AuthViewModel", "   Usuario: $nombreUsuario")
                Log.d("AuthViewModel", "   Correo: $correo")

                // Registrar en backend
                val request = CreateUsuarioRequest(
                    nombreUsuario = nombreUsuario,
                    correo = correo,
                    contrasena = contrasena
                )
                val response = RetrofitClient.authApi.createUsuario(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val usuarioDto = response.body()!!
                    Log.d("AuthViewModel", "Registro exitoso en backend!")
                    Log.d("AuthViewModel", "   ID generado: ${usuarioDto.id}")
                    Log.d("AuthViewModel", "   Nombre usuario: ${usuarioDto.nombreUsuario}")
                    Log.d("AuthViewModel", "   Correo: ${usuarioDto.correo}")

                    // Validar que los campos necesarios no sean null
                    if (usuarioDto.nombreUsuario.isNullOrBlank() || usuarioDto.correo.isNullOrBlank()) {
                        Log.e("AuthViewModel", "Error: El usuario del backend tiene campos null o vacíos")
                        Log.e("AuthViewModel", "   nombreUsuario: '${usuarioDto.nombreUsuario}'")
                        Log.e("AuthViewModel", "   correo: '${usuarioDto.correo}'")
                        _uiState.update {
                            it.copy(
                                authState = AuthState.ERROR,
                                errorMessage = "Error: El servidor devolvió datos inválidos"
                            )
                        }
                        liberarOperacion("register")
                        return@launch
                    }

                    // Guardar en la base de datos local CON EL MISMO ID del backend
                    try {
                        Log.d("AuthViewModel", "Guardando usuario en BD local con ID: ${usuarioDto.id}")
                        repository.insertarUsuarioConId(
                            id = usuarioDto.id,
                            nombreUsuario = usuarioDto.nombreUsuario,
                            correo = usuarioDto.correo,
                            contrasena = contrasena
                        )
                        Log.d("AuthViewModel", "Usuario guardado exitosamente en BD local")
                    } catch (e: Exception) {
                        Log.w("AuthViewModel", "Error guardando usuario localmente: ${e.message}")
                        e.printStackTrace()
                        // Si falla (ya existe), intentar obtenerlo
                    }

                    // Crear la entidad local para el estado
                    val localUser = UsuarioEntity(
                        id = usuarioDto.id,
                        nombreUsuario = usuarioDto.nombreUsuario,
                        correo = usuarioDto.correo,
                        contrasena = contrasena
                    )

                    // Guardar sesión
                    try {
                        sessionManager.saveCurrentUserId(usuarioDto.id)
                        Log.d("AuthViewModel", "Sesión guardada para usuario ID: ${usuarioDto.id}")
                    } catch (_: Exception) { }

                    _uiState.update {
                        AuthUiState(authState = AuthState.LOGGED_IN, currentUser = localUser)
                    }
                } else {
                    // Manejar diferentes códigos de error
                    val errorBodyString = try {
                        response.errorBody()?.string() ?: ""
                    } catch (e: Exception) {
                        ""
                    }
                    Log.e("AuthViewModel", "Error en registro backend: ${response.code()}")
                    Log.e("AuthViewModel", "Error body: $errorBodyString")
                    
                    val errorMessage = obtenerMensajeError(response.code(), errorBodyString)
                    
                    _uiState.update {
                        it.copy(
                            authState = AuthState.ERROR,
                            errorMessage = errorMessage
                        )
                    }
                }
                liberarOperacion("register")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error general en registro: ${e.message}")
                e.printStackTrace()
                
                // Determinar mensaje de error amigable
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ->
                        "No se puede conectar al servidor. Verifica tu conexión a internet."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "La conexión tardó demasiado. Por favor, intenta de nuevo."
                    else ->
                        "Error al registrar usuario. Por favor, intenta de nuevo más tarde."
                }
                
                _uiState.update {
                    it.copy(authState = AuthState.ERROR, errorMessage = errorMessage)
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
    
    /**
     * Convierte códigos de error HTTP en mensajes claros y comprensibles para el usuario
     */
    private fun obtenerMensajeError(statusCode: Int, errorBody: String?): String {
        // Intentar extraer mensaje del JSON de error del servidor
        val errorBodyString = errorBody ?: ""
        try {
            if (errorBodyString.isNotBlank()) {
                val gson = com.google.gson.Gson()
                val errorMap = gson.fromJson(errorBodyString, Map::class.java) as? Map<*, *>
                val serverMessage = errorMap?.get("error")?.toString()
                if (!serverMessage.isNullOrBlank()) {
                    return serverMessage
                }
            }
        } catch (e: Exception) {
            // Si no se puede parsear el JSON, continuar con los mensajes por defecto
        }
        
        // Mensajes amigables según el código de error
        return when (statusCode) {
            400 -> {
                if (errorBodyString.contains("nombre", ignoreCase = true)) {
                    "El nombre de usuario debe tener al menos 3 caracteres"
                } else if (errorBodyString.contains("correo", ignoreCase = true)) {
                    "El formato del correo electrónico no es válido"
                } else if (errorBodyString.contains("contraseña", ignoreCase = true) || errorBodyString.contains("contrasena", ignoreCase = true)) {
                    "La contraseña debe tener al menos 6 caracteres"
                } else {
                    "Datos inválidos. Verifica que:\n• El nombre de usuario tenga al menos 3 caracteres\n• El correo sea válido\n• La contraseña tenga al menos 6 caracteres"
                }
            }
            401 -> "Correo o contraseña incorrectos"
            403 -> "No tienes permisos para realizar esta acción"
            404 -> "El recurso solicitado no existe"
            409 -> {
                when {
                    errorBodyString.contains("correo", ignoreCase = true) -> 
                        "El correo electrónico ya está registrado. Por favor, usa otro correo o inicia sesión."
                    errorBodyString.contains("nombre", ignoreCase = true) || errorBodyString.contains("usuario", ignoreCase = true) -> 
                        "El nombre de usuario ya está registrado. Por favor, elige otro nombre de usuario."
                    else -> 
                        "El usuario o correo electrónico ya está registrado. Por favor, usa otros datos o inicia sesión."
                }
            }
            422 -> "Los datos proporcionados no son válidos"
            500 -> "Error en el servidor. Por favor, intenta de nuevo más tarde"
            502 -> "El servidor no está disponible temporalmente. Intenta más tarde"
            503 -> "El servicio no está disponible en este momento. Intenta más tarde"
            504 -> "La conexión tardó demasiado. Por favor, intenta de nuevo"
            else -> "Error de conexión. Por favor, verifica tu internet e intenta de nuevo"
        }
    }
}
