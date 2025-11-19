package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.network.sync.SyncService
import com.example.forcetrack.network.sync.SyncStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la sincronización
 */
data class SyncUiState(
    val isSyncing: Boolean = false,
    val lastSyncStats: SyncStats? = null,
    val errorMessage: String? = null,
    val isConnected: Boolean = false
)

/**
 * ViewModel para manejar la sincronización de datos
 */
class SyncViewModel(private val syncService: SyncService) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    /**
     * Verifica la conexión con el servidor
     */
    fun checkConnection() {
        viewModelScope.launch {
            val isConnected = syncService.checkConnection()
            _uiState.value = _uiState.value.copy(isConnected = isConnected)
        }
    }

    /**
     * Sincroniza todos los datos del usuario
     */
    fun syncAllData(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)

            val result = syncService.fullSync(usuarioId)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncStats = result.getOrNull(),
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error en sincronización"
                )
            }
        }
    }

    /**
     * Sincroniza bloques desde el servidor
     */
    fun syncFromRemote(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)

            val result = syncService.syncBloquesFromRemote(usuarioId)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al descargar datos"
                )
            }
        }
    }

    /**
     * Sincroniza un bloque específico al servidor
     */
    fun syncBloqueToRemote(bloqueId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)

            val result = syncService.syncBloqueToRemote(bloqueId)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al subir bloque"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

