package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.network.api.UsuarioDto
import com.example.forcetrack.network.api.BloqueDto
import com.example.forcetrack.network.repository.XanoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de ejemplo para usar Xano
 * Demuestra las mejores prácticas con coroutines y StateFlow
 */
class XanoViewModel : ViewModel() {

    private val repository = XanoRepository()

    // Estado de UI
    private val _uiState = MutableStateFlow<XanoUiState>(XanoUiState.Idle)
    val uiState: StateFlow<XanoUiState> = _uiState.asStateFlow()

    // Usuario actual
    private val _currentUser = MutableStateFlow<UsuarioDto?>(null)
    val currentUser: StateFlow<UsuarioDto?> = _currentUser.asStateFlow()

    // Lista de bloques
    private val _bloques = MutableStateFlow<List<BloqueDto>>(emptyList())
    val bloques: StateFlow<List<BloqueDto>> = _bloques.asStateFlow()

    // ========== AUTENTICACIÓN ==========

    /**
     * Iniciar sesión
     * USO: viewModel.login("testuser", "123456")
     */
    fun login(nombreUsuario: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = XanoUiState.Loading

            repository.login(nombreUsuario, contrasena)
                .onSuccess { usuario ->
                    _currentUser.value = usuario
                    _uiState.value = XanoUiState.Success("Login exitoso")

                    // Cargar bloques del usuario automáticamente
                    cargarBloques(usuario.id)
                }
                .onFailure { error ->
                    _uiState.value = XanoUiState.Error(
                        error.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

    /**
     * Registrar nuevo usuario
     * USO: viewModel.register("nuevouser", "email@test.com", "password123")
     */
    fun register(nombreUsuario: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = XanoUiState.Loading

            repository.register(nombreUsuario, correo, contrasena)
                .onSuccess { usuario ->
                    _currentUser.value = usuario
                    _uiState.value = XanoUiState.Success("Usuario registrado")
                }
                .onFailure { error ->
                    _uiState.value = XanoUiState.Error(
                        error.message ?: "Error al registrar usuario"
                    )
                }
        }
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        _currentUser.value = null
        _bloques.value = emptyList()
        _uiState.value = XanoUiState.Idle
    }

    // ========== BLOQUES ==========

    /**
     * Cargar bloques de un usuario
     * USO: viewModel.cargarBloques(usuarioId)
     */
    fun cargarBloques(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.value = XanoUiState.Loading

            repository.obtenerBloques(usuarioId)
                .onSuccess { bloquesList ->
                    _bloques.value = bloquesList
                    _uiState.value = XanoUiState.Success("${bloquesList.size} bloques cargados")
                }
                .onFailure { error ->
                    _uiState.value = XanoUiState.Error(
                        error.message ?: "Error al cargar bloques"
                    )
                }
        }
    }

    /**
     * Crear un nuevo bloque
     * USO: viewModel.crearBloque("Bloque Fuerza 2025")
     */
    fun crearBloque(nombre: String) {
        val usuarioId = _currentUser.value?.id ?: return

        viewModelScope.launch {
            _uiState.value = XanoUiState.Loading

            repository.crearBloque(usuarioId, nombre)
                .onSuccess { nuevoBloque ->
                    // Agregar a la lista existente
                    _bloques.value = _bloques.value + nuevoBloque
                    _uiState.value = XanoUiState.Success("Bloque creado")
                }
                .onFailure { error ->
                    _uiState.value = XanoUiState.Error(
                        error.message ?: "Error al crear bloque"
                    )
                }
        }
    }

    /**
     * Eliminar un bloque
     * USO: viewModel.eliminarBloque(bloqueId)
     */
    fun eliminarBloque(bloqueId: Int) {
        viewModelScope.launch {
            _uiState.value = XanoUiState.Loading

            repository.eliminarBloque(bloqueId)
                .onSuccess {
                    // Remover de la lista
                    _bloques.value = _bloques.value.filter { it.id != bloqueId }
                    _uiState.value = XanoUiState.Success("Bloque eliminado")
                }
                .onFailure { error ->
                    _uiState.value = XanoUiState.Error(
                        error.message ?: "Error al eliminar bloque"
                    )
                }
        }
    }

    /**
     * Resetear estado de UI
     */
    fun resetUiState() {
        _uiState.value = XanoUiState.Idle
    }
}

/**
 * Estados posibles de la UI
 */
sealed class XanoUiState {
    object Idle : XanoUiState()
    object Loading : XanoUiState()
    data class Success(val message: String) : XanoUiState()
    data class Error(val message: String) : XanoUiState()
}

