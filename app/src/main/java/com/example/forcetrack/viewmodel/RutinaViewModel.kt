package com.example.forcetrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.model.EjercicioRutina
import com.example.forcetrack.model.Serie
import com.example.forcetrack.network.RetrofitClient
import com.example.forcetrack.network.dto.CreateEjercicioRequest
import com.example.forcetrack.network.dto.CreateSerieRequest
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

                // Asegurar que currentDiaId esté actualizado
                if (currentDiaId != diaEntity.id) {
                    currentDiaId = diaEntity.id
                    Log.d("RutinaViewModel", "✅ currentDiaId actualizado a ${diaEntity.id}")
                }
                
                // Actualizar info del día inmediatamente
                _diaActual.value = DiaRutina(
                    id = diaEntity.id,
                    bloqueId = diaEntity.bloqueId,
                    nombre = diaEntity.nombre,
                    notas = diaEntity.notas,
                    ejercicios = mutableListOf(),
                    fecha = diaEntity.fecha,
                    numeroSemana = diaEntity.numeroSemana,
                    completado = diaEntity.completado,
                    fechaCompletado = diaEntity.fechaCompletado
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

                // 2. SINCRONIZAR CON BACKEND EN SEGUNDO PLANO (no bloqueante)
                sincronizarEjerciciosConBackend(diaId)

            } catch (e: Exception) {
                Log.e("RutinaViewModel", " Error cargando día: ${e.message}")
                e.printStackTrace()
                _mensajeError.value = "Error cargando rutina: ${e.message}"
                _cargando.value = false
            }
        }
    }

    // Sincronizar ejercicios desde backend a BD local en segundo plano
    private fun sincronizarEjerciciosConBackend(diaId: Int) {
        // TODO: Implementar sincronización cuando se necesite
        Log.d("RutinaViewModel", " Sincronización con backend deshabilitada")
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

                // 4. Verificar si el bloque es público y sincronizar
                val bloqueId = repository.obtenerDiaPorId(dia.id)?.bloqueId
                if (bloqueId != null) {
                    val bloque = repository.obtenerBloquePorId(bloqueId)
                    if (bloque?.esPublico == true) {
                        Log.d("RutinaViewModel", "Bloque es público, sincronizando ejercicio al backend...")
                        subirEjercicioAlBackend(ejercicioId, serieId, dia.id, bloqueId, nombreEjercicio, 90)
                    }
                }

            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Error creando ejercicio localmente: ${e.message}")
                _mensajeError.value = "Error: ${e.message}"
            }
        }
    }

    private fun subirEjercicioAlBackend(
        ejercicioIdLocal: Int,
        serieIdLocal: Int,
        diaIdLocal: Int,
        bloqueIdLocal: Int,
        nombreEjercicio: String,
        descanso: Int
    ) {
        viewModelScope.launch {
            try {
                // Buscar el día en el backend
                val bloqueLocal = repository.obtenerBloquePorId(bloqueIdLocal) ?: return@launch
                val bloquesBackendResponse = RetrofitClient.bloqueApi.getAllBloques(bloqueLocal.usuarioId)
                
                if (!bloquesBackendResponse.isSuccessful) return@launch
                
                val bloquesBackend = bloquesBackendResponse.body() ?: return@launch
                val bloqueBackend = bloquesBackend.find { 
                    it.nombre == bloqueLocal.nombre && it.usuarioId == bloqueLocal.usuarioId 
                } ?: return@launch
                
                // Obtener días del bloque backend
                val diasBackendResponse = RetrofitClient.diaApi.getDiasByBloque(bloqueBackend.id)
                if (!diasBackendResponse.isSuccessful) return@launch
                
                val diasBackend = diasBackendResponse.body() ?: return@launch
                val diaLocal = repository.obtenerDiaPorId(diaIdLocal) ?: return@launch
                val diaBackend = diasBackend.find { it.nombre == diaLocal.nombre } ?: return@launch
                
                Log.d("RutinaViewModel", "Subiendo ejercicio '$nombreEjercicio' al backend...")

                // 1. Crear ejercicio en el backend
                val ejercicioRequest = CreateEjercicioRequest(
                    diaId = diaBackend.id,
                    nombre = nombreEjercicio,
                    descansoSegundos = descanso
                )

                val ejercicioResponse = RetrofitClient.ejercicioApi.createEjercicio(ejercicioRequest)

                if (ejercicioResponse.isSuccessful) {
                    val ejercicioDto = ejercicioResponse.body()
                    Log.d("RutinaViewModel", "✅ Ejercicio sincronizado. Backend ID: ${ejercicioDto?.id}")

                    // 2. Crear serie inicial en el backend
                    ejercicioDto?.id?.let { backendEjercicioId ->
                        val serieRequest = CreateSerieRequest(
                            ejercicioId = backendEjercicioId,
                            peso = 0.0,
                            repeticiones = 0,
                            rir = 0
                        )

                        val serieResponse = RetrofitClient.serieApi.createSerie(serieRequest)

                        if (serieResponse.isSuccessful) {
                            Log.d("RutinaViewModel", "✅ Serie inicial sincronizada. Backend ID: ${serieResponse.body()?.id}")
                        } else {
                            Log.e("RutinaViewModel", "❌ Error al sincronizar serie inicial: ${serieResponse.code()}")
                        }
                    }

                } else {
                    Log.e("RutinaViewModel", "❌ Error al sincronizar ejercicio: ${ejercicioResponse.code()} - ${ejercicioResponse.message()}")
                }

            } catch (e: Exception) {
                Log.e("RutinaViewModel", "❌ Error de red al sincronizar ejercicio: ${e.message}")
                // No mostramos error al usuario, es sincronización en segundo plano
            }
        }
    }

    fun eliminarEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            try {
                Log.d("RutinaViewModel", "Eliminando ejercicio $ejercicioId...")
                // Por ahora solo eliminar localmente, backend no tiene el endpoint implementado
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

                // 6. Lanzar subida al backend en coroutine separada (totalmente en segundo plano)
                viewModelScope.launch {
                    try {
                        val serieRequest = com.example.forcetrack.network.dto.CreateSerieRequest(
                            ejercicioId = ejercicioId,
                            peso = 0.0,
                            repeticiones = 0,
                            rir = 0,
                            completada = false
                        )

                        val response = com.example.forcetrack.network.RetrofitClient.serieApi.createSerie(serieRequest)
                        if (response.isSuccessful) {
                            Log.d("RutinaViewModel", "Serie creada en servidor con ID: ${response.body()?.id}")
                        } else {
                            Log.w("RutinaViewModel", "Error creando serie en servidor: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.w("RutinaViewModel", "Error creando serie en servidor: ${e.message}")
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
                    viewModelScope.launch {
                        try {
                            val response = com.example.forcetrack.network.RetrofitClient.serieApi.deleteSerie(serieId)
                            if (response.isSuccessful) {
                                Log.d("RutinaViewModel", "Serie eliminada del servidor")
                            } else {
                                Log.e("RutinaViewModel", "Error eliminando serie en servidor: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("RutinaViewModel", "Error eliminando serie en servidor: ${e.message}")
                            _mensajeError.value = "Advertencia: la serie fue eliminada localmente, pero no en el servidor"
                        }
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

                // 3. Sincronizar con backend en segundo plano (no bloqueante)
                viewModelScope.launch {
                    try {
                        val updateRequest = com.example.forcetrack.network.dto.UpdateSerieRequest(
                            peso = serie.peso,
                            repeticiones = serie.reps,
                            rir = serie.rir,
                            completada = serie.completada
                        )
                        val response = com.example.forcetrack.network.RetrofitClient.serieApi.updateSerie(serie.id, updateRequest)
                        if (response.isSuccessful) {
                            Log.d("RutinaViewModel", "Serie sincronizada con servidor")
                        } else {
                            Log.w("RutinaViewModel", "Error sincronizando con servidor: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.w("RutinaViewModel", "Error sincronizando con servidor: ${e.message}")
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

                // TODO: Agregar actualización en backend cuando implementes el endpoint PATCH para días
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
    
    // Estado para el proceso de terminar día
    private val _terminandoDia = MutableStateFlow(false)
    val terminandoDia: StateFlow<Boolean> = _terminandoDia.asStateFlow()
    
    private val _estadisticasDia = MutableStateFlow<com.example.forcetrack.network.dto.EstadisticasDiaDto?>(null)
    val estadisticasDia: StateFlow<com.example.forcetrack.network.dto.EstadisticasDiaDto?> = _estadisticasDia.asStateFlow()
    
    fun limpiarEstadisticas() {
        _estadisticasDia.value = null
    }
    
    /**
     * Termina/completa el día de entrenamiento actual.
     * Marca el día como completado y genera estadísticas.
     */
    fun terminarDia() {
        Log.d("RutinaViewModel", "🔵 terminarDia() llamado")
        
        // Intentar obtener el ID del día actual de múltiples formas
        val diaId = currentDiaId ?: _diaActual.value?.id
        Log.d("RutinaViewModel", "🔵 currentDiaId = $currentDiaId, diaActual.id = ${_diaActual.value?.id}, usando diaId = $diaId")
        
        if (diaId == null || diaId <= 0) {
            _mensajeError.value = "No hay un día cargado. Por favor, recarga la pantalla."
            Log.e("RutinaViewModel", "❌ No hay día cargado para terminar. diaId es null o inválido")
            return
        }
        
        // Asegurar que currentDiaId esté actualizado
        if (currentDiaId == null || currentDiaId != diaId) {
            currentDiaId = diaId
            Log.d("RutinaViewModel", "🔵 Actualizado currentDiaId a $diaId")
        }
        
        viewModelScope.launch {
            Log.d("RutinaViewModel", "🔵 Iniciando coroutine para terminar día")
            _terminandoDia.value = true
            _mensajeError.value = ""
            
            try {
                Log.d("RutinaViewModel", "🏁 Terminando día $diaId...")
                
                // Intentar primero con PUT /terminar (endpoint recomendado)
                try {
                    val response = RetrofitClient.diaApi.terminarDia(diaId)
                    Log.d("RutinaViewModel", "Respuesta PUT /terminar: código ${response.code()}, exitosa: ${response.isSuccessful}")
                    
                    if (response.isSuccessful && response.body() != null) {
                        val resultado = response.body()!!
                        Log.d("RutinaViewModel", "Resultado: exito=${resultado.exito}, mensaje=${resultado.mensaje}")
                        
                        if (resultado.exito && resultado.estadisticas != null) {
                            Log.d("RutinaViewModel", "✅ Día terminado exitosamente: ${resultado.mensaje}")
                            
                            val stats = resultado.estadisticas!!
                            _estadisticasDia.value = stats
                            
                            // Actualizar el día en el estado local
                            _diaActual.update { dia ->
                                dia?.copy(
                                    completado = true,
                                    fechaCompletado = stats.fechaCompletado
                                )
                            }
                            
                            // Recargar el día para obtener los datos actualizados
                            cargarDia(diaId)
                            return@launch
                        } else {
                            val mensajeError = resultado.mensaje ?: "No se pudo completar el día"
                            Log.w("RutinaViewModel", "⚠️ Error: $mensajeError")
                            _mensajeError.value = mensajeError
                            return@launch
                        }
                    } else {
                        val errorBody = try {
                            response.errorBody()?.string() ?: "Error desconocido"
                        } catch (e: Exception) {
                            "No se pudo leer el error"
                        }
                        Log.w("RutinaViewModel", "PUT /terminar falló con código ${response.code()}, body: $errorBody")
                    }
                } catch (e1: Exception) {
                    Log.w("RutinaViewModel", "PUT /terminar falló con excepción: ${e1.message}", e1)
                }
                
                // Si PUT falla, usar POST /completar (endpoint alternativo)
                try {
                    Log.d("RutinaViewModel", "Intentando POST /completar como alternativa...")
                    val postResponse = RetrofitClient.diaApi.completarDia(diaId)
                    Log.d("RutinaViewModel", "Respuesta POST /completar: código ${postResponse.code()}, exitosa: ${postResponse.isSuccessful}")
                    
                    if (postResponse.isSuccessful && postResponse.body() != null) {
                        val stats = postResponse.body()!!
                        Log.d("RutinaViewModel", "✅ Día completado usando endpoint POST")
                        _estadisticasDia.value = stats
                        
                        _diaActual.update { dia ->
                            dia?.copy(
                                completado = true,
                                fechaCompletado = stats.fechaCompletado
                            )
                        }
                        
                        cargarDia(diaId)
                    } else {
                        val errorBody = try {
                            postResponse.errorBody()?.string() ?: "Error desconocido"
                        } catch (e: Exception) {
                            "No se pudo leer el error"
                        }
                        Log.e("RutinaViewModel", "❌ Error al terminar día: ${postResponse.code()} - $errorBody")
                        _mensajeError.value = "Error al terminar el día. Verifica tu conexión e intenta de nuevo."
                    }
                } catch (e2: Exception) {
                    Log.e("RutinaViewModel", "❌ POST /completar también falló: ${e2.message}", e2)
                    _mensajeError.value = "Error de conexión. Verifica tu internet e intenta de nuevo."
                }
            } catch (e: Exception) {
                Log.e("RutinaViewModel", "❌ Excepción general al terminar día: ${e.message}", e)
                _mensajeError.value = "Error al terminar el día. Por favor, intenta de nuevo."
            } finally {
                _terminandoDia.value = false
            }
        }
    }
    
    /**
     * Obtiene el estado del día para verificar si puede completarse.
     */
    fun verificarEstadoDia() {
        val diaId = currentDiaId ?: return
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.diaApi.obtenerEstadoDia(diaId)
                if (response.isSuccessful && response.body() != null) {
                    val estado = response.body()!!
                    Log.d("RutinaViewModel", "Estado del día: ${estado.mensaje}")
                    // Puedes usar este estado para mostrar información al usuario
                }
            } catch (e: Exception) {
                Log.e("RutinaViewModel", "Error verificando estado: ${e.message}")
            }
        }
    }
}
