package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.model.EjercicioRutina
import com.example.forcetrack.model.Serie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RutinaViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _diaActual = MutableStateFlow<DiaRutina?>(null)
    val diaActual: StateFlow<DiaRutina?> = _diaActual.asStateFlow()

    private val _ejercicios = MutableStateFlow<List<EjercicioRutina>>(emptyList())
    val ejercicios: StateFlow<List<EjercicioRutina>> = _ejercicios.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _mensajeError = MutableStateFlow("")
    val mensajeError: StateFlow<String> = _mensajeError.asStateFlow()

    fun cargarDia(diaId: Int) {
        if (diaId == _diaActual.value?.id) return // No recargar si ya está cargado
        
        viewModelScope.launch {
            _cargando.value = true
            try {
                val diaEntity = repository.obtenerDiaPorId(diaId) ?: throw IllegalStateException("Día no encontrado")
                val ejerciciosEntity = repository.obtenerEjercicios(diaId).first()

                val ejerciciosCompletos = ejerciciosEntity.map { ejercicioEntity ->
                    val seriesEntity = repository.obtenerSeries(ejercicioEntity.id).first()
                    val series = seriesEntity.map { serieEntity ->
                        Serie(id = serieEntity.id, ejercicioId = serieEntity.ejercicioId, peso = serieEntity.peso, reps = serieEntity.repeticiones, rir = serieEntity.rir, completada = serieEntity.completada)
                    }.toMutableList()
                    EjercicioRutina(id = ejercicioEntity.id, diaId = ejercicioEntity.diaId, nombre = ejercicioEntity.nombre, descanso = ejercicioEntity.descansoSegundos, series = series)
                }

                _diaActual.value = DiaRutina(id = diaEntity.id, semanaId = diaEntity.semanaId, nombre = diaEntity.nombre, notas = diaEntity.notas, ejercicios = ejerciciosCompletos.toMutableList())
                _ejercicios.value = ejerciciosCompletos

            } catch (e: Exception) {
                _mensajeError.value = "Error al cargar el día: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Las demás funciones (agregarEjercicio, eliminarSerie, etc.) permanecen igual
    // ya que operan sobre el estado local (_ejercicios) que ya está cargado.
    
    fun agregarEjercicio(nombreEjercicio: String) {
        val dia = _diaActual.value ?: return
        viewModelScope.launch {
            try {
                val ejercicioId = repository.agregarEjercicio(dia.id, nombreEjercicio)
                val serieId = repository.agregarSerie(ejercicioId.toInt())

                val nuevaSerie = Serie(id = serieId.toInt(), ejercicioId = ejercicioId.toInt())
                val nuevoEjercicio = EjercicioRutina(
                    id = ejercicioId.toInt(),
                    diaId = dia.id,
                    nombre = nombreEjercicio,
                    series = mutableListOf(nuevaSerie)
                )
                _ejercicios.update { it + nuevoEjercicio }
            } catch (e: Exception) {
                _mensajeError.value = "Error al agregar el ejercicio: ${e.message}"
            }
        }
    }

    fun eliminarEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarEjercicio(ejercicioId)
                _ejercicios.update { it.filterNot { ej -> ej.id == ejercicioId } }
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar el ejercicio: ${e.message}"
            }
        }
    }

    fun agregarSerie(ejercicioId: Int) {
        viewModelScope.launch {
            try {
                val nuevaSerieId = repository.agregarSerie(ejercicioId)
                val nuevaSerie = Serie(id = nuevaSerieId.toInt(), ejercicioId = ejercicioId)

                _ejercicios.update { listaActual ->
                    listaActual.map {
                        if (it.id == ejercicioId) {
                            val nuevasSeries = it.series.toMutableList().apply { add(nuevaSerie) }
                            it.copy(series = nuevasSeries)
                        } else {
                            it
                        }
                    }
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error al agregar la serie: ${e.message}"
            }
        }
    }

    fun eliminarSerie(ejercicioId: Int, serieId: Int) {
        viewModelScope.launch {
            try {
                val ejercicio = _ejercicios.value.first { it.id == ejercicioId }
                if (ejercicio.series.size > 1) {
                    repository.eliminarSerie(serieId)

                    _ejercicios.update { listaActual ->
                        listaActual.map {
                            if (it.id == ejercicioId) {
                                val nuevasSeries = it.series.filterNot { s -> s.id == serieId }.toMutableList()
                                it.copy(series = nuevasSeries)
                            } else {
                                it
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar la serie: ${e.message}"
            }
        }
    }

    fun actualizarSerie(serie: Serie) {
        viewModelScope.launch {
            try {
                repository.actualizarSerie(serie.id, serie.peso, serie.reps, serie.rir, serie.completada)

                _ejercicios.update { listaActual ->
                    listaActual.map {
                        if (it.id == serie.ejercicioId) {
                            val nuevasSeries = it.series.map { if (it.id == serie.id) serie else it }.toMutableList()
                            it.copy(series = nuevasSeries)
                        } else {
                            it
                        }
                    }
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error al actualizar la serie: ${e.message}"
            }
        }
    }

    fun actualizarDescanso(ejercicioId: Int, descanso: Int) {
        viewModelScope.launch {
            try {
                repository.actualizarDescansoEjercicio(ejercicioId, descanso)

                _ejercicios.update { listaActual ->
                    listaActual.map {
                        if (it.id == ejercicioId) {
                            it.copy(descanso = descanso)
                        } else {
                            it
                        }
                    }
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error al actualizar el descanso: ${e.message}"
            }
        }
    }

    fun actualizarNotas(notas: String) {
        val dia = _diaActual.value ?: return
        viewModelScope.launch {
            try {
                repository.actualizarNotasDia(dia.id, notas)
                _diaActual.update { it?.copy(notas = notas) }
            } catch (e: Exception) {
                _mensajeError.value = "Error al actualizar las notas: ${e.message}"
            }
        }
    }
}
