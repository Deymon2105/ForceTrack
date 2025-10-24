package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.TrainingLogEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainingLogUiState(
    val logs: List<TrainingLogEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TrainingLogViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingLogUiState())
    val uiState: StateFlow<TrainingLogUiState> = _uiState.asStateFlow()

    fun loadLogs(usuarioId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.obtenerLogsUsuario(usuarioId).collect { logs ->
                    _uiState.value = TrainingLogUiState(logs = logs)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    suspend fun getLogByDate(usuarioId: Int, dateIso: String): TrainingLogEntity? {
        return repository.obtenerLogPorFecha(usuarioId, dateIso)
    }

    fun saveLog(log: TrainingLogEntity) {
        viewModelScope.launch {
            try {
                repository.crearOActualizarLog(log)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteLogById(id: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarLogPorId(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}

