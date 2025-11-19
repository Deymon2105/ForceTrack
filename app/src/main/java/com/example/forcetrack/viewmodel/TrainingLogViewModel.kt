package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.TrainingLogEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class TrainingLogUiState(
    val logs: List<TrainingLogEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TrainingLogViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingLogUiState())
    val uiState: StateFlow<TrainingLogUiState> = _uiState.asStateFlow()

    private var logsCollectJob: Job? = null
    private var currentUsuarioId: Int? = null

    fun loadLogs(usuarioId: Int) {
        // Si ya estamos observando este usuario, no crear otro collector
        if (currentUsuarioId == usuarioId && logsCollectJob?.isActive == true) {
            return
        }

        // Cancelar cualquier recolección previa
        logsCollectJob?.cancel()
        currentUsuarioId = usuarioId

        logsCollectJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.obtenerLogsUsuario(usuarioId)
                .catch { e ->
                    _uiState.value = TrainingLogUiState(
                        logs = emptyList(),
                        isLoading = false,
                        errorMessage = e.message ?: "Error desconocido"
                    )
                }
                .collect { logs ->
                    _uiState.value = TrainingLogUiState(
                        logs = logs,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    suspend fun getLogByDate(usuarioId: Int, dateIso: String): TrainingLogEntity? {
        return try {
            repository.obtenerLogPorFecha(usuarioId, dateIso)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message)
            null
        }
    }

    fun saveLog(log: TrainingLogEntity) {
        viewModelScope.launch {
            try {
                repository.crearOActualizarLog(log)
                // El Flow actualizará automáticamente la lista
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteLogById(id: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarLogPorId(id)
                // El Flow actualizará automáticamente la lista
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        logsCollectJob?.cancel()
    }
}
