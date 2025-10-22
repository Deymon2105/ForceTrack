package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.UsuarioEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
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

class AuthViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(nombreUsuario: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = AuthState.LOADING) }
            try {
                val user = repository.iniciarSesion(nombreUsuario, contrasena)
                if (user != null) {
                    _uiState.update { AuthUiState(authState = AuthState.LOGGED_IN, currentUser = user) }
                } else {
                    _uiState.update { it.copy(authState = AuthState.ERROR, errorMessage = "Usuario o contraseña incorrectos") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(authState = AuthState.ERROR, errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun register(nombreUsuario: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = AuthState.LOADING) }
            try {
                if (repository.usuarioExiste(nombreUsuario)) {
                    _uiState.update { it.copy(authState = AuthState.ERROR, errorMessage = "El nombre de usuario ya existe") }
                    return@launch
                }
                val userId = repository.registrarUsuario(nombreUsuario, correo, contrasena)
                val user = repository.obtenerUsuarioPorId(userId.toInt())
                _uiState.update { AuthUiState(authState = AuthState.LOGGED_IN, currentUser = user) }
            } catch (e: Exception) {
                _uiState.update { it.copy(authState = AuthState.ERROR, errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, authState = AuthState.LOGGED_OUT) }
    }
}
