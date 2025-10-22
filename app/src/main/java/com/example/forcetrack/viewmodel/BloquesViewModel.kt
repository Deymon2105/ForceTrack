package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.BloqueEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

// Estado para la UI de la pantalla de Bloques
data class BloquesUiState(
    val bloques: List<BloqueEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BloquesViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BloquesUiState())
    val uiState: StateFlow<BloquesUiState> = _uiState.asStateFlow()

    // Carga los bloques para un usuario específico
    fun cargarBloques(usuarioId: Int) {
        viewModelScope.launch {
            repository.obtenerBloques(usuarioId)
                .onStart { _uiState.value = BloquesUiState(isLoading = true) }
                // CORREGIDO: El typo "BlolesUiState" se ha corregido a "BloquesUiState".
                .catch { e -> _uiState.value = BloquesUiState(errorMessage = e.message) }
                .collect { bloques ->
                    _uiState.value = BloquesUiState(bloques = bloques)
                }
        }
    }

    // Crea un nuevo bloque, con sus semanas y días
    // Ahora acepta el número de días por semana (por defecto 7). Se valida el rango 1..7.
    fun crearBloque(nombre: String, usuarioId: Int, numeroSemanas: Int, numeroDiasPorSemana: Int = 7) {
        viewModelScope.launch {
            try {
                val bloqueId = repository.crearBloque(nombre, usuarioId)
                // Aseguramos un rango válido de días por semana
                val diasPorSemana = numeroDiasPorSemana.coerceIn(1, 7)
                for (i in 1..numeroSemanas) {
                    val semanaId = repository.crearSemana(bloqueId.toInt(), i)
                    // Creamos 'diasPorSemana' días para cada semana
                    for (d in 1..diasPorSemana) {
                        val nombreDia = "Día $d"
                        repository.crearDia(semanaId.toInt(), nombreDia)
                    }
                }
                // La recarga de bloques se hará automáticamente gracias al Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al crear el bloque: ${e.message}")
            }
        }
    }

    // Elimina un bloque de entrenamiento
    fun eliminarBloque(bloqueId: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarBloque(bloqueId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al eliminar el bloque: ${e.message}")
            }
        }
    }
}