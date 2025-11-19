package com.example.forcetrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.model.EjercicioRutina
import com.example.forcetrack.model.Serie
import com.example.forcetrack.network.repository.RemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Repositorio remoto para Xano
    private val remoteRepository = RemoteRepository()
    private var currentDiaId: Int? = null

    // Control para evitar spam en operaciones de series
    private val _operacionesEnProceso = MutableStateFlow<Set<String>>(emptySet())
    val operacionesEnProceso: StateFlow<Set<String>> = _operacionesEnProceso.asStateFlow()

    // Helper para verificar si una operación está en proceso
    fun isOperacionEnProceso(ejercicioId: Int, tipo: String): Boolean {
        return _operacionesEnProceso.value.contains("${tipo}_$ejercicioId")
    }

    private fun marcarOperacionEnProceso(ejercicioId: Int, tipo: String) {
        _operacionesEnProceso.update { it + "${tipo}_$ejercicioId" }
    }

    private fun liberarOperacion(ejercicioId: Int, tipo: String) {
        _operacionesEnProceso.update { it - "${tipo}_$ejercicioId" }
    }

    fun cargarDia(diaId: Int) {
        currentDiaId = diaId

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = ""

            try {
                Log.d("RutinaViewModel", "Cargando día $diaId desde Xano...")

                // Cargar día desde BD local primero (para info básica)
                val diaEntity = repository.obtenerDiaPorId(diaId)
                if (diaEntity != null) {
                    _diaActual.value = DiaRutina(
                        id = diaEntity.id,
                        semanaId = diaEntity.semanaId,
                        nombre = diaEntity.nombre,
                        notas = diaEntity.notas,
                        ejercicios = mutableListOf()
                    )
                }

                // Cargar ejercicios desde Xano
                val xanoRepo = com.example.forcetrack.network.repository.XanoRepository()
                xanoRepo.obtenerEjercicios(diaId)
                    .onSuccess { ejerciciosDto ->
                        Log.d("RutinaViewModel", "Ejercicios cargados: ${ejerciciosDto.size}")

                        // Cargar series para cada ejercicio
                        val ejerciciosCompletos = ejerciciosDto.map { ejercicioDto ->
                            val series = mutableListOf<Serie>()

                            xanoRepo.obtenerSeries(ejercicioDto.id)
                                .onSuccess { seriesDto ->
                                    seriesDto.forEach { serieDto ->
                                        series.add(
                                            Serie(
                                                id = serieDto.id,
                                                ejercicioId = serieDto.ejercicioId,
                                                peso = serieDto.peso,
                                                reps = serieDto.repeticiones,
                                                rir = serieDto.rir,
                                                completada = serieDto.completada
                                            )
                                        )
                                    }
                                }

                            EjercicioRutina(
                                id = ejercicioDto.id,
                                diaId = ejercicioDto.diaId,
                                nombre = ejercicioDto.nombre,
                                descanso = ejercicioDto.descansoSegundos,
                                series = series
                            )
                        }

                        _ejercicios.value = ejerciciosCompletos
                        _cargando.value = false
                    }
                    .onFailure { error ->
                        Log.e("RutinaViewModel", "Error cargando ejercicios: ${error.message}")
                        _mensajeError.value = "Error: ${error.message}"
                        _cargando.value = false
                    }

            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Error general: ${e.message}")
                _mensajeError.value = "Error: ${e.message}"
                _cargando.value = false
            }
        }
    }

    fun agregarEjercicio(nombreEjercicio: String) {
        val dia = _diaActual.value ?: return

        if (nombreEjercicio.isBlank()) {
            _mensajeError.value = "El nombre del ejercicio no puede estar vacío"
            return
        }

        viewModelScope.launch {
            try {
                Log.d("RutinaViewModel", "Agregando ejercicio '$nombreEjercicio' a Xano...")

                val ejercicioDto = com.example.forcetrack.network.dto.EjercicioDto(
                    id = 0,
                    diaId = dia.id,
                    nombre = nombreEjercicio,
                    descansoSegundos = 90,
                    series = null
                )

                remoteRepository.createEjercicio(ejercicioDto)
                    .onSuccess { ejercicioCreado ->
                        Log.d("RutinaViewModel", "Ejercicio creado con ID: ${ejercicioCreado.id}")

                        // Crear una serie inicial
                        val serieDto = com.example.forcetrack.network.dto.SerieDto(
                            id = 0,
                            ejercicioId = ejercicioCreado.id,
                            peso = 0.0,
                            repeticiones = 0,
                            rir = 0,
                            completada = false
                        )

                        remoteRepository.createSerie(serieDto)
                            .onSuccess {
                                // Recargar el día
                                cargarDia(dia.id)
                            }
                    }
                    .onFailure { error ->
                        Log.e("RutinaViewModel", "Error: ${error.message}")
                        _mensajeError.value = "Error: ${error.message}"
                    }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    fun eliminarEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            try {
                Log.d("RutinaViewModel", "Eliminando ejercicio $ejercicioId...")
                // Por ahora solo eliminar localmente, Xano no tiene el endpoint implementado
                repository.eliminarEjercicio(ejercicioId)
                currentDiaId?.let { cargarDia(it) }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    fun agregarSerie(ejercicioId: Int) {
        // ANTI-SPAM: Evitar múltiples clicks
        if (isOperacionEnProceso(ejercicioId, "agregar_serie")) {
            Log.d("RutinaViewModel", "Operación ya en proceso, ignorando click...")
            return
        }

        viewModelScope.launch {
            try {
                // Marcar operación en proceso
                marcarOperacionEnProceso(ejercicioId, "agregar_serie")

                Log.d("RutinaViewModel", "Agregando serie al ejercicio $ejercicioId...")

                // 1. Crear serie optimista en el estado local INMEDIATAMENTE
                val tempId = -(System.currentTimeMillis().toInt()) // ID temporal negativo
                val nuevaSerie = Serie(
                    id = tempId,
                    ejercicioId = ejercicioId,
                    peso = 0.0,
                    reps = 0,
                    rir = 0,
                    completada = false
                )

                // 2. Actualizar UI inmediatamente (optimistic update)
                _ejercicios.update { listaActual ->
                    listaActual.map { ejercicio ->
                        if (ejercicio.id == ejercicioId) {
                            val nuevasSeries = ejercicio.series.toMutableList()
                            nuevasSeries.add(nuevaSerie)
                            ejercicio.copy(series = nuevasSeries)
                        } else {
                            ejercicio
                        }
                    }
                }

                // 3. Guardar en servidor en segundo plano
                val serieDto = com.example.forcetrack.network.dto.SerieDto(
                    id = 0,
                    ejercicioId = ejercicioId,
                    peso = 0.0,
                    repeticiones = 0,
                    rir = 0,
                    completada = false
                )

                remoteRepository.createSerie(serieDto)
                    .onSuccess { serieCreada ->
                        Log.d("RutinaViewModel", "Serie creada en servidor con ID: ${serieCreada.id}")

                        // 4. Actualizar el ID temporal con el ID real
                        _ejercicios.update { listaActual ->
                            listaActual.map { ejercicio ->
                                if (ejercicio.id == ejercicioId) {
                                    val nuevasSeries = ejercicio.series.map { serie ->
                                        if (serie.id == tempId) {
                                            serie.copy(id = serieCreada.id)
                                        } else {
                                            serie
                                        }
                                    }.toMutableList()
                                    ejercicio.copy(series = nuevasSeries)
                                } else {
                                    ejercicio
                                }
                            }
                        }

                        // Liberar operación
                        liberarOperacion(ejercicioId, "agregar_serie")
                    }
                    .onFailure { error ->
                        Log.e("RutinaViewModel", "Error creando serie: ${error.message}")
                        // Revertir cambio optimista en caso de error
                        _ejercicios.update { listaActual ->
                            listaActual.map { ejercicio ->
                                if (ejercicio.id == ejercicioId) {
                                    val nuevasSeries = ejercicio.series.filter { it.id != tempId }.toMutableList()
                                    ejercicio.copy(series = nuevasSeries)
                                } else {
                                    ejercicio
                                }
                            }
                        }
                        _mensajeError.value = "Error al agregar serie: ${error.message}"

                        // Liberar operación
                        liberarOperacion(ejercicioId, "agregar_serie")
                    }
            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Excepción: ${e.message}")
                _mensajeError.value = "Error: ${e.message}"

                // Liberar operación en caso de excepción
                liberarOperacion(ejercicioId, "agregar_serie")
            }
        }
    }

    fun eliminarSerie(ejercicioId: Int, serieId: Int) {
        viewModelScope.launch {
            try {
                val ejercicio = _ejercicios.value.firstOrNull { it.id == ejercicioId }
                if (ejercicio == null) {
                    _mensajeError.value = "Ejercicio no encontrado"
                    return@launch
                }

                if (ejercicio.series.size <= 1) {
                    _mensajeError.value = "No puedes eliminar la última serie"
                    return@launch
                }

                Log.d("RutinaViewModel", "Eliminando serie $serieId...")

                // 1. Guardar la serie para posible rollback
                val serieEliminada = ejercicio.series.firstOrNull { it.id == serieId }

                // 2. Eliminar de la UI inmediatamente (optimistic update)
                _ejercicios.update { listaActual ->
                    listaActual.map { ej ->
                        if (ej.id == ejercicioId) {
                            val nuevasSeries = ej.series.filter { it.id != serieId }.toMutableList()
                            ej.copy(series = nuevasSeries)
                        } else {
                            ej
                        }
                    }
                }

                // 3. Eliminar del servidor en segundo plano (solo si no es ID temporal)
                if (serieId > 0) {
                    remoteRepository.deleteSerie(serieId)
                        .onSuccess {
                            Log.d("RutinaViewModel", "Serie eliminada del servidor")
                            // También eliminar de BD local
                            repository.eliminarSerie(serieId)
                        }
                        .onFailure { error ->
                            Log.e("RutinaViewModel", "Error eliminando serie: ${error.message}")
                            // Revertir cambio optimista si falla
                            if (serieEliminada != null) {
                                _ejercicios.update { listaActual ->
                                    listaActual.map { ej ->
                                        if (ej.id == ejercicioId) {
                                            val nuevasSeries = ej.series.toMutableList()
                                            nuevasSeries.add(serieEliminada)
                                            ej.copy(series = nuevasSeries)
                                        } else {
                                            ej
                                        }
                                    }
                                }
                            }
                            _mensajeError.value = "Error al eliminar serie: ${error.message}"
                        }
                } else {
                    // Si es ID temporal, solo eliminar localmente
                    Log.d("RutinaViewModel", "Serie temporal eliminada")
                }
            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Excepción: ${e.message}")
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    fun actualizarSerie(serie: Serie) {
        viewModelScope.launch {
            try {
                Log.d("RutinaViewModel", "Actualizando serie ${serie.id} en Xano...")

                remoteRepository.updateSerie(serie.id, com.example.forcetrack.network.dto.SerieDto(
                    id = serie.id,
                    ejercicioId = serie.ejercicioId,
                    peso = serie.peso,
                    repeticiones = serie.reps,
                    rir = serie.rir,
                    completada = serie.completada
                ))
                    .onSuccess {
                        Log.d("RutinaViewModel", "Serie actualizada")
                        // Actualizar en el estado local para UX inmediata
                        _ejercicios.update { listaActual ->
                            listaActual.map { ejercicio ->
                                if (ejercicio.id == serie.ejercicioId) {
                                    val nuevasSeries = ejercicio.series.map {
                                        if (it.id == serie.id) serie else it
                                    }.toMutableList()
                                    ejercicio.copy(series = nuevasSeries)
                                } else {
                                    ejercicio
                                }
                            }
                        }
                    }
                    .onFailure { error ->
                        Log.e("RutinaViewModel", "Error: ${error.message}")
                        _mensajeError.value = "Error: ${error.message}"
                    }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
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
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    fun actualizarNotas(notas: String) {
        viewModelScope.launch {
            try {
                val dia = _diaActual.value ?: return@launch
                Log.d("RutinaViewModel", "Actualizando notas del día ${dia.id}...")

                // Actualizar en el estado local inmediatamente
                _diaActual.update { it?.copy(notas = notas) }

                // Actualizar en la BD local
                repository.actualizarNotasDia(dia.id, notas)

                // TODO: Agregar actualización en Xano cuando implementes el endpoint PATCH para días
                Log.d("RutinaViewModel", "Notas actualizadas localmente")
            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Error actualizando notas: ${e.message}")
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    fun limpiarError() {
        _mensajeError.value = ""
    }
}
