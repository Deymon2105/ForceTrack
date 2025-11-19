package com.example.forcetrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.entity.BloqueEntity
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.network.repository.RemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Repositorio remoto para Xano
    private val remoteRepository = RemoteRepository()

    private var currentUsuarioId: Int? = null

    // ⛔ ANTI-SPAM: Control de operaciones en proceso
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

    // Carga los bloques para un usuario específico desde Xano
    fun cargarBloques(usuarioId: Int) {
        currentUsuarioId = usuarioId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("BloquesViewModel", "Obteniendo bloques desde Xano para usuario $usuarioId...")

                remoteRepository.getBloques(usuarioId)
                    .onSuccess { bloquesDto ->
                        Log.d("BloquesViewModel", "Bloques obtenidos: ${bloquesDto.size}")

                        // Convertir DTOs a entities para la UI
                        val bloques = bloquesDto.map { dto ->
                            BloqueEntity(
                                id = dto.id,
                                usuarioId = dto.usuarioId,
                                nombre = dto.nombre
                            )
                        }

                        // Actualizar también la BD local para cache
                        bloques.forEach { bloque ->
                            try {
                                repository.crearBloque(bloque.nombre, bloque.usuarioId)
                            } catch (e: Exception) {
                                // Ya existe, ignorar
                            }
                        }

                        _uiState.value = BloquesUiState(
                            bloques = bloques,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    .onFailure { error ->
                        Log.e("BloquesViewModel", "Error obteniendo bloques: ${error.message}")
                        // Intentar cargar desde BD local como fallback
                        repository.obtenerBloques(usuarioId).collect { bloquesLocales ->
                            _uiState.value = BloquesUiState(
                                bloques = bloquesLocales,
                                isLoading = false,
                                errorMessage = "Modo offline: ${error.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error general: ${e.message}")
                _uiState.value = BloquesUiState(
                    bloques = emptyList(),
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    // Crea un nuevo bloque en Xano con sus semanas y días
    fun crearBloque(nombre: String, usuarioId: Int, numeroSemanas: Int, numeroDiasPorSemana: Int = 7) {
        // ANTI-SPAM: Evitar múltiples clicks al crear bloque
        if (isOperacionEnProceso("crear_bloque")) {
            Log.d("BloquesViewModel", "Creación de bloque ya en proceso, ignorando...")
            return
        }

        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El nombre del bloque no puede estar vacío")
            return
        }

        if (numeroSemanas <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "El número de semanas debe ser mayor a 0")
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("crear_bloque")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("BloquesViewModel", "Creando bloque '$nombre' con RequestQueue automático...")

                // Crear bloque en Xano
                val bloqueDto = com.example.forcetrack.network.dto.BloqueDto(
                    id = 0,
                    usuarioId = usuarioId,
                    nombre = nombre,
                    semanas = null
                )

                remoteRepository.createBloque(bloqueDto)
                    .onSuccess { bloqueCreado ->
                        Log.d("BloquesViewModel", "Bloque creado con ID: ${bloqueCreado.id}")

                        val diasPorSemana = numeroDiasPorSemana.coerceIn(1, 7)

                        // NUEVO: SIN delays manuales - RequestQueue controla automáticamente
                        for (i in 1..numeroSemanas) {
                            val semanaDto = com.example.forcetrack.network.dto.SemanaDto(
                                id = 0,
                                bloqueId = bloqueCreado.id,
                                numeroSemana = i,
                                dias = null
                            )

                            remoteRepository.createSemana(semanaDto)
                                .onSuccess { semanaCreada ->
                                    Log.d("BloquesViewModel", "Semana $i creada")

                                    // Crear días para esta semana
                                    for (d in 1..diasPorSemana) {
                                        val diaDto = com.example.forcetrack.network.dto.DiaDto(
                                            id = 0,
                                            semanaId = semanaCreada.id,
                                            nombre = "Día $d",
                                            notas = null,
                                            ejercicios = null
                                        )

                                        remoteRepository.createDia(diaDto)
                                            .onSuccess {
                                                Log.d("BloquesViewModel", "Día $d creado")
                                            }
                                            .onFailure { error ->
                                                Log.e("BloquesViewModel", "Error creando día: ${error.message}")
                                            }
                                    }
                                }
                                .onFailure { error ->
                                    Log.e("BloquesViewModel", "Error creando semana: ${error.message}")
                                }
                        }

                        // Pequeño delay solo antes de recargar
                        kotlinx.coroutines.delay(500)

                        // Recargar bloques
                        cargarBloques(usuarioId)
                        liberarOperacion("crear_bloque")
                    }
                    .onFailure { error ->
                        Log.e("BloquesViewModel", "Error creando bloque: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al crear el bloque: ${error.message}"
                        )
                        liberarOperacion("crear_bloque")
                    }

            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error general: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
                liberarOperacion("crear_bloque")
            }
        }
    }

    // Elimina un bloque en Xano
    fun eliminarBloque(bloqueId: Int) {
        // ANTI-SPAM: Evitar múltiples clicks al eliminar bloque
        if (isOperacionEnProceso("eliminar_$bloqueId")) {
            Log.d("BloquesViewModel", "Eliminación de bloque ya en proceso, ignorando...")
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("eliminar_$bloqueId")
            try {
                _uiState.value = _uiState.value.copy(errorMessage = null)

                Log.d("BloquesViewModel", "Eliminando bloque $bloqueId en Xano...")

                remoteRepository.deleteBloque(bloqueId)
                    .onSuccess {
                        Log.d("BloquesViewModel", "Bloque eliminado")
                        // Recargar bloques
                        currentUsuarioId?.let { cargarBloques(it) }
                    }
                    .onFailure { error ->
                        Log.e("BloquesViewModel", "Error eliminando bloque: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al eliminar: ${error.message}"
                        )
                    }
                    liberarOperacion("eliminar_$bloqueId")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error: ${e.message}"
                )
                liberarOperacion("eliminar_$bloqueId")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}