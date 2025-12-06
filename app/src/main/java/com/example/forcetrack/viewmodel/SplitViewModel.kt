package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.model.BloqueEntrenamiento
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.model.EjercicioRutina
import com.example.forcetrack.model.Serie
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplitUiState(
    val bloque: BloqueEntrenamiento? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SplitViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitUiState())
    val uiState: StateFlow<SplitUiState> = _uiState.asStateFlow()

    // Mantener jobs por día para suscribirnos a los flows de ejercicios de cada día
    private val diaJobs = mutableMapOf<Int, Job>()

    // ⛔ ANTI-SPAM: Control de operaciones en proceso
    private val _operacionesEnProceso = MutableStateFlow<Set<String>>(emptySet())

    fun isOperacionEnProceso(operacion: String): Boolean {
        return _operacionesEnProceso.value.contains(operacion)
    }

    private fun marcarOperacionEnProceso(operacion: String) {
        _operacionesEnProceso.update { it + operacion }
    }

    private fun liberarOperacion(operacion: String) {
        _operacionesEnProceso.update { it - operacion }
    }

    fun loadBlockDetails(bloqueId: Int) {
        // Cancelar jobs anteriores
        diaJobs.values.forEach { it.cancel() }
        diaJobs.clear()

        viewModelScope.launch {
            _uiState.value = SplitUiState(isLoading = true)
            try {
                val bloqueEntity = repository.obtenerBloquePorId(bloqueId) ?: throw IllegalStateException("Bloque no encontrado")
                
                // Fetch days directly for the block
                val diasEntity = repository.obtenerDias(bloqueId).first()
                
                val dias = diasEntity.map { diaEntity ->
                    // Cargamos ejercicios y series para cada día (snapshot inicial)
                    val ejerciciosEntity = repository.obtenerEjercicios(diaEntity.id).first()
                    val ejercicios = ejerciciosEntity.map { ejercicioEntity ->
                        val seriesEntity = repository.obtenerSeries(ejercicioEntity.id).first()
                        val series = seriesEntity.map { serieEntity ->
                            Serie(id = serieEntity.id, ejercicioId = serieEntity.ejercicioId, peso = serieEntity.peso, reps = serieEntity.repeticiones, rir = serieEntity.rir, completada = serieEntity.completada)
                        }.toMutableList()
                        EjercicioRutina(id = ejercicioEntity.id, diaId = ejercicioEntity.diaId, nombre = ejercicioEntity.nombre, series = series, descanso = ejercicioEntity.descansoSegundos)
                    }.toMutableList()

                    DiaRutina(id = diaEntity.id, bloqueId = diaEntity.bloqueId, nombre = diaEntity.nombre, notas = diaEntity.notas, ejercicios = ejercicios, fecha = diaEntity.fecha, numeroSemana = diaEntity.numeroSemana)
                }.toMutableList()

                val bloqueCompleto = BloqueEntrenamiento(
                    id = bloqueEntity.id,
                    nombre = bloqueEntity.nombre,
                    usuarioId = bloqueEntity.usuarioId,
                    dias = dias
                )

                _uiState.value = SplitUiState(bloque = bloqueCompleto)

                // Ahora suscribirnos a cambios en los ejercicios por cada día
                bloqueCompleto.dias.forEach { dia ->
                    // Evitar duplicar collectors
                    if (diaJobs.containsKey(dia.id)) return@forEach

                    val job = viewModelScope.launch {
                        repository.obtenerEjercicios(dia.id).collect { ejerciciosEntity ->
                            // Para cada ejercicio obtener sus series (snapshot) y actualizar el día en el estado
                            val ejercicios = ejerciciosEntity.map { ejercicioEntity ->
                                val seriesEntity = repository.obtenerSeries(ejercicioEntity.id).first()
                                val series = seriesEntity.map { serieEntity ->
                                    Serie(id = serieEntity.id, ejercicioId = serieEntity.ejercicioId, peso = serieEntity.peso, reps = serieEntity.repeticiones, rir = serieEntity.rir, completada = serieEntity.completada)
                                }.toMutableList()
                                EjercicioRutina(id = ejercicioEntity.id, diaId = ejercicioEntity.diaId, nombre = ejercicioEntity.nombre, series = series, descanso = ejercicioEntity.descansoSegundos)
                            }.toMutableList()

                            // Actualizar el estado reemplazando las ejercicios del día correspondiente
                            val current = _uiState.value.bloque
                            if (current != null) {
                                val nuevosDias = current.dias.map { d ->
                                    if (d.id == dia.id) {
                                        d.copy(ejercicios = ejercicios)
                                    } else d
                                }.toMutableList()

                                val nuevoBloque = current.copy(dias = nuevosDias)
                                _uiState.value = _uiState.value.copy(bloque = nuevoBloque)
                            }
                        }
                    }
                    diaJobs[dia.id] = job
                }

            } catch (e: Exception) {
                _uiState.value = SplitUiState(errorMessage = "Error al cargar el bloque: ${e.message}")
            }
        }
    }

    fun crearDia(bloqueId: Int, nombre: String) {
        viewModelScope.launch {
            try {
                repository.crearDia(bloqueId, nombre)
                // Reload block details to update the list
                loadBlockDetails(bloqueId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al crear día: ${e.message}")
            }
        }
    }

    fun eliminarDia(diaId: Int, bloqueId: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarDia(diaId)
                loadBlockDetails(bloqueId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al eliminar día: ${e.message}")
            }
        }
    }

    fun eliminarBloque(bloqueId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.eliminarBloque(bloqueId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al eliminar bloque: ${e.message}")
            }
        }
    }
}