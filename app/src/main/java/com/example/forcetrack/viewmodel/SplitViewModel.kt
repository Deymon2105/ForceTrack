package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.model.BloqueEntrenamiento
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.model.EjercicioRutina
import com.example.forcetrack.model.Serie
import com.example.forcetrack.model.SemanaEntrenamiento
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    fun loadBlockDetails(bloqueId: Int) {
        // Cancelar jobs anteriores
        diaJobs.values.forEach { it.cancel() }
        diaJobs.clear()

        viewModelScope.launch {
            _uiState.value = SplitUiState(isLoading = true)
            try {
                val bloqueEntity = repository.obtenerBloquePorId(bloqueId) ?: throw IllegalStateException("Bloque no encontrado")
                val semanasEntity = repository.obtenerSemanas(bloqueId).first()
                val semanas = semanasEntity.map { semanaEntity ->
                    val diasEntity = repository.obtenerDias(semanaEntity.id).first()
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

                        DiaRutina(id = diaEntity.id, semanaId = diaEntity.semanaId, nombre = diaEntity.nombre, notas = diaEntity.notas, ejercicios = ejercicios)
                    }.toMutableList()
                    SemanaEntrenamiento(id = semanaEntity.id, bloqueId = semanaEntity.bloqueId, numero = semanaEntity.numeroSemana, dias = dias)
                }.toMutableList()

                val bloqueCompleto = BloqueEntrenamiento(
                    id = bloqueEntity.id,
                    nombre = bloqueEntity.nombre,
                    usuarioId = bloqueEntity.usuarioId,
                    semanas = semanas
                )

                _uiState.value = SplitUiState(bloque = bloqueCompleto)

                // Ahora suscribirnos a cambios en los ejercicios por cada día
                bloqueCompleto.semanas.forEach { semana ->
                    semana.dias.forEach { dia ->
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
                                    val nuevasSemanas = current.semanas.map { s ->
                                        if (s.id == semana.id) {
                                            val nuevosDias = s.dias.map { d ->
                                                if (d.id == dia.id) {
                                                    d.copy(ejercicios = ejercicios)
                                                } else d
                                            }.toMutableList()
                                            s.copy(dias = nuevosDias)
                                        } else s
                                    }.toMutableList()

                                    val nuevoBloque = current.copy(semanas = nuevasSemanas)
                                    _uiState.value = _uiState.value.copy(bloque = nuevoBloque)
                                }
                            }
                        }
                        diaJobs[dia.id] = job
                    }
                }

            } catch (e: Exception) {
                _uiState.value = SplitUiState(errorMessage = "Error al cargar el bloque: ${e.message}")
            }
        }
    }
}