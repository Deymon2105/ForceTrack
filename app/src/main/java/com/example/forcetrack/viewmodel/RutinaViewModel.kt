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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay

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
                Log.d("RutinaViewModel", "⚡ Cargando día $diaId desde BD local...")

                // 1. CARGAR DESDE BD LOCAL PRIMERO (instantáneo y optimizado)
                val diaEntity = repository.obtenerDiaPorId(diaId)
                if (diaEntity == null) {
                    _mensajeError.value = "Día no encontrado"
                    _cargando.value = false
                    return@launch
                }

                // Actualizar info del día inmediatamente
                _diaActual.value = DiaRutina(
                    id = diaEntity.id,
                    semanaId = diaEntity.semanaId,
                    nombre = diaEntity.nombre,
                    notas = diaEntity.notas,
                    ejercicios = mutableListOf()
                )

                // Cargar ejercicios con series (usando first() en vez de collect para evitar bucles)
                val ejerciciosEntity = repository.obtenerEjercicios(diaId).first()
                Log.d("RutinaViewModel", "✅ ${ejerciciosEntity.size} ejercicios encontrados localmente")

                val ejerciciosCompletos = ejerciciosEntity.map { ejercicioEntity ->
                    // Cargar series de cada ejercicio
                    val seriesEntity = repository.obtenerSeries(ejercicioEntity.id).first()
                    val series = seriesEntity.map { serieEntity ->
                        Serie(
                            id = serieEntity.id,
                            ejercicioId = serieEntity.ejercicioId,
                            peso = serieEntity.peso,
                            reps = serieEntity.repeticiones,
                            rir = serieEntity.rir,
                            completada = serieEntity.completada
                        )
                    }.toMutableList()

                    EjercicioRutina(
                        id = ejercicioEntity.id,
                        diaId = ejercicioEntity.diaId,
                        nombre = ejercicioEntity.nombre,
                        descanso = ejercicioEntity.descansoSegundos,
                        series = series
                    )
                }

                _ejercicios.value = ejerciciosCompletos
                _cargando.value = false
                Log.d("RutinaViewModel", " Día cargado completamente desde BD local")

                // 2. SINCRONIZAR CON XANO EN SEGUNDO PLANO (no bloqueante)
                sincronizarEjerciciosConXano(diaId)

            } catch (e: Exception) {
                Log.e("RutinaViewModel", " Error cargando día: ${e.message}")
                e.printStackTrace()
                _mensajeError.value = "Error cargando rutina: ${e.message}"
                _cargando.value = false
            }
        }
    }

    // Sincronizar ejercicios desde Xano a BD local en segundo plano
    private fun sincronizarEjerciciosConXano(diaId: Int) {
        viewModelScope.launch {
            try {
                Log.d("RutinaViewModel", " Sincronizando con Xano en segundo plano...")

                val xanoRepo = com.example.forcetrack.network.repository.XanoRepository()
                xanoRepo.obtenerEjercicios(diaId)
                    .onSuccess { ejerciciosDto ->
                        Log.d("RutinaViewModel", "📥 ${ejerciciosDto.size} ejercicios en Xano")
                        // La sincronización es pasiva, no afecta la UI
                    }
                    .onFailure { error ->
                        Log.w("RutinaViewModel", "⚠ No se pudo sincronizar: ${error.message}")
                    }
            } catch (e: Exception) {
                Log.w("RutinaViewModel", " Error en sincronización: ${e.message}")
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
                Log.d("RutinaViewModel", "Agregando ejercicio '$nombreEjercicio' localmente primero...")

                // 1. CREAR EN BD LOCAL PRIMERO (instantáneo)
                val ejercicioIdLong = repository.agregarEjercicio(dia.id, nombreEjercicio)
                val ejercicioId = ejercicioIdLong.toInt()
                Log.d("RutinaViewModel", "Ejercicio creado localmente con ID: $ejercicioId")

                // 2. Crear una serie inicial en BD local
                val serieIdLong = repository.agregarSerie(ejercicioId)
                val serieId = serieIdLong.toInt()
                Log.d("RutinaViewModel", "Serie inicial creada localmente con ID: $serieId")

                // 3. Actualizar UI inmediatamente con datos locales
                val nuevaSerie = Serie(
                    id = serieId,
                    ejercicioId = ejercicioId,
                    peso = 0.0,
                    reps = 0,
                    rir = 0,
                    completada = false
                )

                val nuevoEjercicio = EjercicioRutina(
                    id = ejercicioId,
                    diaId = dia.id,
                    nombre = nombreEjercicio,
                    descanso = 90,
                    series = mutableListOf(nuevaSerie)
                )

                _ejercicios.update { listaActual ->
                    listaActual + nuevoEjercicio
                }

                // 4. SUBIR A XANO EN SEGUNDO PLANO (no bloqueante)
                subirEjercicioAXano(ejercicioId, dia.id, nombreEjercicio, 90)

            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Error creando ejercicio localmente: ${e.message}")
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    // Subir ejercicio y serie inicial a Xano en segundo plano
    private fun subirEjercicioAXano(ejercicioIdLocal: Int, diaId: Int, nombreEjercicio: String, descanso: Int) {
        viewModelScope.launch {
            try {
                Log.d("RutinaViewModel", "Subiendo ejercicio '$nombreEjercicio' a Xano en segundo plano...")

                val ejercicioDto = com.example.forcetrack.network.dto.EjercicioDto(
                    id = 0,
                    diaId = diaId,
                    nombre = nombreEjercicio,
                    descansoSegundos = descanso,
                    series = null
                )

                remoteRepository.createEjercicio(ejercicioDto)
                    .onSuccess { ejercicioCreado ->
                        Log.d("RutinaViewModel", "Ejercicio subido a Xano con ID: ${ejercicioCreado.id}")

                        // Crear serie inicial en Xano
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
                                Log.d("RutinaViewModel", "Serie inicial subida a Xano")
                            }
                            .onFailure { error ->
                                Log.w("RutinaViewModel", "Error subiendo serie a Xano: ${error.message}")
                            }
                    }
                    .onFailure { error ->
                        Log.w("RutinaViewModel", "Error subiendo ejercicio a Xano: ${error.message}")
                        // No mostrar error al usuario, el ejercicio ya está guardado localmente
                    }
            } catch (e: Exception) {
                Log.w("RutinaViewModel", "Excepción subiendo a Xano: ${e.message}")
                // No afecta la experiencia del usuario
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

                // 1. Crear serie optimista en el estado local INMEDIATAMENTE con ID temporal
                val tempId = -(System.currentTimeMillis().toInt()) // ID temporal negativo
                val nuevaSerieOpt = Serie(
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
                            nuevasSeries.add(nuevaSerieOpt)
                            ejercicio.copy(series = nuevasSeries)
                        } else {
                            ejercicio
                        }
                    }
                }

                // 3. Guardar en BD local (esto evita que la serie se pierda al salir de la app)
                val serieIdLong = try {
                    repository.agregarSerie(ejercicioId)
                } catch (dbEx: Exception) {
                    Log.e("RutinaViewModel", "Error guardando serie en BD local: ${dbEx.message}")
                    // Revertir cambio optimista si falla el guardado local
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
                    _mensajeError.value = "Error guardando serie localmente: ${dbEx.message}"
                    liberarOperacion(ejercicioId, "agregar_serie")
                    return@launch
                }

                val serieId = serieIdLong.toInt()

                // 4. Reemplazar ID temporal por ID real en el estado local
                _ejercicios.update { listaActual ->
                    listaActual.map { ejercicio ->
                        if (ejercicio.id == ejercicioId) {
                            val nuevasSeries = ejercicio.series.map { serie ->
                                if (serie.id == tempId) serie.copy(id = serieId) else serie
                            }.toMutableList()
                            ejercicio.copy(series = nuevasSeries)
                        } else {
                            ejercicio
                        }
                    }
                }

                Log.d("RutinaViewModel", "Serie guardada localmente con ID: $serieId")

                // 5. Esperar 800ms y liberar el botón INMEDIATAMENTE
                delay(800)
                liberarOperacion(ejercicioId, "agregar_serie")
                Log.d("RutinaViewModel", " Botón desbloqueado - listo para agregar otra serie")

                // 6. Lanzar subida a Xano en coroutine separada (totalmente en segundo plano)
                viewModelScope.launch {
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
                        }
                        .onFailure { error ->
                            Log.w("RutinaViewModel", "Error creando serie en servidor: ${error.message}")
                        }
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
                    try {
                        // Eliminar localmente primero para que no reaparezca al recargar
                        repository.eliminarSerie(serieId)
                    } catch (dbEx: Exception) {
                        Log.e("RutinaViewModel", "Error eliminando serie en BD local: ${dbEx.message}")
                        // Revertir cambio optimista si no pudimos eliminar localmente
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
                        _mensajeError.value = "Error eliminando serie localmente: ${dbEx.message}"
                        return@launch
                    }

                    // Intentar eliminar en el servidor en segundo plano; si falla, solo loguear
                    remoteRepository.deleteSerie(serieId)
                        .onSuccess {
                            Log.d("RutinaViewModel", "Serie eliminada del servidor")
                        }
                        .onFailure { error ->
                            Log.e("RutinaViewModel", "Error eliminando serie en servidor: ${error.message}")
                            _mensajeError.value = "Advertencia: la serie fue eliminada localmente, pero no en el servidor"
                        }
                } else {
                    // Si es ID temporal, solo eliminar localmente (ya se removió del estado)
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
                Log.d("RutinaViewModel", "Actualizando serie ${serie.id} - Peso: ${serie.peso}, Reps: ${serie.reps}, RIR: ${serie.rir}")

                // 1. Actualizar en el estado local INMEDIATAMENTE para UX fluida
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

                // 2. Guardar en BD local para persistencia
                try {
                    repository.actualizarSerie(
                        serieId = serie.id,
                        peso = serie.peso,
                        repeticiones = serie.reps,
                        rir = serie.rir,
                        completada = serie.completada
                    )
                    Log.d("RutinaViewModel", " Serie actualizada en BD local")
                } catch (dbEx: Exception) {
                    Log.e("RutinaViewModel", "Error actualizando en BD local: ${dbEx.message}")
                }

                // 3. Sincronizar con Xano en segundo plano (no bloqueante)
                viewModelScope.launch {
                    remoteRepository.updateSerie(serie.id, com.example.forcetrack.network.dto.SerieDto(
                        id = serie.id,
                        ejercicioId = serie.ejercicioId,
                        peso = serie.peso,
                        repeticiones = serie.reps,
                        rir = serie.rir,
                        completada = serie.completada
                    ))
                        .onSuccess {
                            Log.d("RutinaViewModel", " Serie sincronizada con servidor")
                        }
                        .onFailure { error ->
                            Log.w("RutinaViewModel", " Error sincronizando con servidor: ${error.message}")
                        }
                }

            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Error actualizando serie: ${e.message}")
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
