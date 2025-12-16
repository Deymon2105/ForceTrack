package com.example.forcetrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.BloqueEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.network.RetrofitClient
import com.example.forcetrack.network.dto.CreateBloqueRequest
import com.example.forcetrack.network.dto.UpdateBloqueRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

// Estado para la UI de la pantalla de Bloques
data class BloquesUiState(
    val bloques: List<BloqueEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BloquesViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BloquesUiState())
    val uiState: StateFlow<BloquesUiState> = _uiState.asStateFlow()

    private var currentUsuarioId: Int? = null

    // Control de operaciones en proceso
    private val _operacionesEnProceso = MutableStateFlow<Set<String>>(emptySet())

    private fun isOperacionEnProceso(operacion: String): Boolean {
        return _operacionesEnProceso.value.contains(operacion)
    }

    private fun marcarOperacionEnProceso(operacion: String) {
        _operacionesEnProceso.update { it + operacion }
    }

    private fun liberarOperacion(operacion: String) {
        _operacionesEnProceso.update { it - operacion }
    }

    // Carga los bloques desde la base de datos LOCAL y sincroniza con el backend
    fun cargarBloques(usuarioId: Int) {
        currentUsuarioId = usuarioId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("BloquesViewModel", "Cargando bloques locales para usuario $usuarioId...")

                // 1. Cargar desde BD local PRIMERO (instantáneo)
                repository.obtenerBloques(usuarioId).collect { bloquesLocales ->
                    Log.d("BloquesViewModel", "Bloques locales cargados: ${bloquesLocales.size}")
                    _uiState.value = BloquesUiState(
                        bloques = bloquesLocales,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                // 2. Sincronizar con backend EN SEGUNDO PLANO (no bloqueante)
                sincronizarConBackend(usuarioId)

            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error cargando bloques: ${e.message}")
                _uiState.value = BloquesUiState(
                    bloques = emptyList(),
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    // Sincroniza bloques del backend a BD local (en segundo plano)
    private fun sincronizarConBackend(usuarioId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BloquesViewModel", "Sincronizando con backend en segundo plano...")

                val response = RetrofitClient.bloqueApi.getAllBloques(usuarioId)

                if (response.isSuccessful) {
                    val bloquesDto = response.body() ?: emptyList()
                    Log.d("BloquesViewModel", "Bloques del backend obtenidos: ${bloquesDto.size}")

                    // Actualizar BD local con datos del backend
                    bloquesDto.forEach { dto ->
                        try {
                            repository.crearBloque(dto.nombre, dto.usuarioId, dto.categoria)
                        } catch (_: Exception) {
                            // Ya existe, ignorar
                        }
                    }
                } else {
                    Log.w("BloquesViewModel", "No se pudo sincronizar con backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w("BloquesViewModel", "Error en sincronización: ${e.message}")
            }
        }
    }

    // Crea un bloque LOCALMENTE (instantáneo) y lo sube al backend en segundo plano
    fun crearBloque(nombre: String, usuarioId: Int, categoria: String = "General") {
        if (isOperacionEnProceso("crear_bloque")) {
            Log.d("BloquesViewModel", "Creación de bloque ya en proceso, ignorando...")
            return
        }

        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El nombre del bloque no puede estar vacío")
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("crear_bloque")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("BloquesViewModel", "Creando bloque '$nombre' ($categoria) localmente...")

                // 1. CREAR LOCALMENTE PRIMERO (instantáneo)
                val bloqueIdLong = repository.crearBloque(nombre, usuarioId, categoria)
                val bloqueId = bloqueIdLong.toInt()
                Log.d("BloquesViewModel", "Bloque creado localmente con ID: $bloqueId")

                Log.d("BloquesViewModel", "Bloque '$nombre' creado completamente en BD local")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )

                liberarOperacion("crear_bloque")

                // 2. SUBIR AL BACKEND EN SEGUNDO PLANO (no bloqueante)
                subirBloqueAlBackend(bloqueId, nombre, usuarioId, categoria)

            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error creando bloque: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al crear el bloque: ${e.message}"
                )
                liberarOperacion("crear_bloque")
            }
        }
    }

    // Sube el bloque al backend en segundo plano
    private fun subirBloqueAlBackend(bloqueIdLocal: Int, nombre: String, usuarioId: Int, categoria: String) {
        viewModelScope.launch {
            try {
                Log.d("BloquesViewModel", "Subiendo bloque '$nombre' al backend...")

                val request = CreateBloqueRequest(
                    usuarioId = usuarioId,
                    nombre = nombre,
                    categoria = categoria,
                    esPublico = false
                )

                val response = RetrofitClient.bloqueApi.createBloque(request)
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d("BloquesViewModel", "Bloque subido al backend con ID: ${response.body()!!.id}")
                } else {
                    Log.e("BloquesViewModel", "Error subiendo bloque al backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error en subida al backend: ${e.message}")
            }
        }
    }

    // Elimina un bloque LOCALMENTE y del backend
    fun eliminarBloque(bloqueId: Int) {
        if (isOperacionEnProceso("eliminar_$bloqueId")) {
            Log.d("BloquesViewModel", "Eliminación de bloque ya en proceso, ignorando...")
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("eliminar_$bloqueId")
            try {
                _uiState.value = _uiState.value.copy(errorMessage = null)

                Log.d("BloquesViewModel", "Eliminando bloque $bloqueId localmente...")

                // 1. Eliminar de BD local PRIMERO (instantáneo)
                repository.eliminarBloque(bloqueId)
                Log.d("BloquesViewModel", "Bloque eliminado de BD local")

                liberarOperacion("eliminar_$bloqueId")

                // 2. Eliminar del backend EN SEGUNDO PLANO
                eliminarBloqueDelBackend(bloqueId)

            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error eliminando bloque: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar: ${e.message}"
                )
                liberarOperacion("eliminar_$bloqueId")
            }
        }
    }

    // Elimina el bloque del backend en segundo plano (también lo elimina de bloques públicos)
    private fun eliminarBloqueDelBackend(bloqueId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BloquesViewModel", "Eliminando bloque $bloqueId del backend (zona pública)...")

                val response = RetrofitClient.bloqueApi.deleteBloque(bloqueId)
                
                if (response.isSuccessful) {
                    Log.d("BloquesViewModel", "✅ Bloque eliminado del backend y de bloques públicos")
                } else if (response.code() == 404) {
                    Log.w("BloquesViewModel", "⚠️ Bloque no estaba en el backend (ya eliminado o nunca fue público)")
                } else {
                    Log.w("BloquesViewModel", "❌ Error eliminando del backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w("BloquesViewModel", "❌ Error de red eliminando del backend: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Cambia la visibilidad de un bloque entre público y privado
     * PÚBLICO: Crea el bloque en backend con sus datos
     * PRIVADO: Elimina el bloque del backend
     */
    fun cambiarVisibilidad(bloqueId: Int, esPublico: Boolean) {
        if (isOperacionEnProceso("visibilidad_$bloqueId")) {
            Log.d("BloquesViewModel", "Cambio de visibilidad ya en proceso, ignorando...")
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("visibilidad_$bloqueId")
            try {
                Log.d("BloquesViewModel", "Cambiando visibilidad del bloque $bloqueId a ${if (esPublico) "público" else "privado"}...")

                // Usar directamente el endpoint PATCH /bloque/{id}/visibilidad del backend
                // Este endpoint maneja todo: actualiza la visibilidad y sincroniza los datos si es necesario
                val response = RetrofitClient.bloqueApi.cambiarVisibilidadBloque(bloqueId, esPublico)
                
                if (response.isSuccessful && response.body() != null) {
                    val bloqueActualizado = response.body()!!
                    Log.d("BloquesViewModel", "✅ Visibilidad cambiada exitosamente en backend a: ${bloqueActualizado.esPublico}")
                    
                    // Actualizar localmente con los datos del backend
                    repository.actualizarVisibilidadBloque(bloqueId, bloqueActualizado.esPublico)
                    
                    // Recargar bloques para actualizar la UI
                    currentUsuarioId?.let { cargarBloques(it) }
                } else {
                    val errorBody = try {
                        response.errorBody()?.string() ?: "Error desconocido"
                    } catch (e: Exception) {
                        "Error desconocido"
                    }
                    Log.e("BloquesViewModel", "❌ Error cambiando visibilidad: ${response.code()} - $errorBody")
                    // Si falla el backend, aún actualizar localmente para mejor UX
                    repository.actualizarVisibilidadBloque(bloqueId, esPublico)
                    // Recargar bloques para actualizar la UI
                    currentUsuarioId?.let { cargarBloques(it) }
                }

                liberarOperacion("visibilidad_$bloqueId")

            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error cambiando visibilidad: ${e.message}", e)
                // Si falla, aún actualizar localmente
                try {
                    repository.actualizarVisibilidadBloque(bloqueId, esPublico)
                } catch (ex: Exception) {
                    Log.e("BloquesViewModel", "Error actualizando localmente: ${ex.message}")
                }
                liberarOperacion("visibilidad_$bloqueId")
            }
        }
    }
    

    /**
     * Sincronizar días, ejercicios y series de un bloque al backend
     */
    private suspend fun sincronizarDatosAlBackend(bloqueIdLocal: Int, bloqueIdBackend: Int) {
        try {
            Log.d("BloquesViewModel", "Sincronizando días y ejercicios del bloque al backend...")
            
            // Obtener todos los días del bloque
            val dias = repository.obtenerDias(bloqueIdLocal).first()
            
            dias.forEach { diaEntity ->
                // Sincronizar cada día
                val diaRequest = com.example.forcetrack.network.dto.CreateDiaRequest(
                    bloqueId = bloqueIdBackend, // Usar el ID del backend, no el local
                    nombre = diaEntity.nombre,
                    notas = null
                )
                
                val diaResponse = RetrofitClient.diaApi.createDia(diaRequest)
                
                if (diaResponse.isSuccessful) {
                    val diaBackendId = diaResponse.body()?.id ?: return@forEach
                    Log.d("BloquesViewModel", "✅ Día '${diaEntity.nombre}' sincronizado con ID: $diaBackendId")
                    
                    // Obtener ejercicios del día
                    val ejercicios = repository.obtenerEjercicios(diaEntity.id).first()
                    
                    ejercicios.forEach { ejercicioEntity ->
                        // Sincronizar cada ejercicio
                        val ejercicioRequest = com.example.forcetrack.network.dto.CreateEjercicioRequest(
                            diaId = diaBackendId,
                            nombre = ejercicioEntity.nombre,
                            descansoSegundos = ejercicioEntity.descansoSegundos
                        )
                        
                        val ejercicioResponse = RetrofitClient.ejercicioApi.createEjercicio(ejercicioRequest)
                        
                        if (ejercicioResponse.isSuccessful) {
                            val ejercicioBackendId = ejercicioResponse.body()?.id ?: return@forEach
                            Log.d("BloquesViewModel", "  ✅ Ejercicio '${ejercicioEntity.nombre}' sincronizado")
                            
                            // Sincronizar series
                            val series = repository.obtenerSeries(ejercicioEntity.id).first()
                            
                            series.forEach { serieEntity ->
                                val serieRequest = com.example.forcetrack.network.dto.CreateSerieRequest(
                                    ejercicioId = ejercicioBackendId,
                                    peso = serieEntity.peso,
                                    repeticiones = serieEntity.repeticiones,
                                    rir = serieEntity.rir
                                )
                                
                                RetrofitClient.serieApi.createSerie(serieRequest)
                            }
                            
                            Log.d("BloquesViewModel", "    ✅ ${series.size} series sincronizadas")
                        }
                    }
                }
            }
            
            Log.d("BloquesViewModel", "✅ Sincronización completa: ${dias.size} días sincronizados")
        } catch (e: Exception) {
            Log.e("BloquesViewModel", "❌ Error sincronizando datos: ${e.message}")
        }
    }
}