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

    private val remoteRepository = RemoteRepository()
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

    // Carga los bloques desde la base de datos LOCAL (rápido)
    // Y sincroniza con Xano en segundo plano
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

                // 2. Sincronizar con Xano EN SEGUNDO PLANO (no bloqueante)
                sincronizarConXano(usuarioId)

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

    // Sincroniza bloques de Xano a BD local (en segundo plano)
    private fun sincronizarConXano(usuarioId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BloquesViewModel", "Sincronizando con Xano en segundo plano...")

                remoteRepository.getBloques(usuarioId)
                    .onSuccess { bloquesDto ->
                        Log.d("BloquesViewModel", "Bloques de Xano obtenidos: ${bloquesDto.size}")

                        // Actualizar BD local con datos de Xano
                        bloquesDto.forEach { dto ->
                            try {
                                repository.crearBloque(dto.nombre, dto.usuarioId)
                            } catch (_: Exception) {
                                // Ya existe, ignorar
                            }
                        }
                    }
                    .onFailure { error ->
                        Log.w("BloquesViewModel", "No se pudo sincronizar con Xano: ${error.message}")
                        // No mostrar error al usuario, los datos locales ya están
                    }
            } catch (e: Exception) {
                Log.w("BloquesViewModel", "Error en sincronización: ${e.message}")
            }
        }
    }

    // Crea un bloque LOCALMENTE (instantáneo) y lo sube a Xano en segundo plano
    fun crearBloque(nombre: String, usuarioId: Int, numeroSemanas: Int, numeroDiasPorSemana: Int = 7) {
        if (isOperacionEnProceso("crear_bloque")) {
            Log.d("BloquesViewModel", "Creación de bloque ya en proceso, ignorando...")
            return
        }

        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El nombre del bloque no puede estar vacío")
            return
        }

        if (numeroSemanas <= 0 || numeroSemanas > 52) {
            _uiState.value = _uiState.value.copy(errorMessage = "Número de semanas debe estar entre 1 y 52")
            return
        }

        viewModelScope.launch {
            marcarOperacionEnProceso("crear_bloque")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("BloquesViewModel", "Creando bloque '$nombre' localmente...")

                // 1. CREAR LOCALMENTE PRIMERO (instantáneo)
                val bloqueIdLong = repository.crearBloque(nombre, usuarioId)
                val bloqueId = bloqueIdLong.toInt()
                Log.d("BloquesViewModel", "Bloque creado localmente con ID: $bloqueId")

                val diasPorSemana = numeroDiasPorSemana.coerceIn(1, 7)
                val nombresDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

                // Crear semanas y días localmente
                for (numeroSemana in 1..numeroSemanas) {
                    val semanaIdLong = repository.crearSemana(bloqueId, numeroSemana)
                    val semanaId = semanaIdLong.toInt()
                    Log.d("BloquesViewModel", "Semana $numeroSemana creada localmente con ID: $semanaId")

                    for (numeroDia in 1..diasPorSemana) {
                        val nombreDia = if (numeroDia <= nombresDias.size) {
                            nombresDias[numeroDia - 1]
                        } else {
                            "Día $numeroDia"
                        }

                        val diaIdLong = repository.crearDia(semanaId, nombreDia)
                        val diaId = diaIdLong.toInt()
                        Log.d("BloquesViewModel", "Día $nombreDia creado localmente con ID: $diaId")
                    }
                }

                Log.d("BloquesViewModel", "Bloque '$nombre' creado completamente en BD local")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )

                liberarOperacion("crear_bloque")

                // 2. SUBIR A XANO EN SEGUNDO PLANO (no bloqueante)
                subirBloqueAXano(bloqueId, nombre, usuarioId, numeroSemanas, diasPorSemana)

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

    // Sube el bloque y su estructura a Xano en segundo plano
    private fun subirBloqueAXano(bloqueIdLocal: Int, nombre: String, usuarioId: Int, numeroSemanas: Int, diasPorSemana: Int) {
        viewModelScope.launch {
            try {
                Log.d("BloquesViewModel", "Subiendo bloque '$nombre' (ID local: $bloqueIdLocal) a Xano en segundo plano...")

                // Crear bloque en Xano
                val bloqueDto = com.example.forcetrack.network.dto.BloqueDto(
                    id = 0,
                    usuarioId = usuarioId,
                    nombre = nombre,
                    semanas = null
                )

                remoteRepository.createBloque(bloqueDto)
                    .onSuccess { bloqueCreado ->
                        Log.d("BloquesViewModel", "Bloque subido a Xano con ID: ${bloqueCreado.id}")

                        // Crear semanas en Xano
                        for (i in 1..numeroSemanas) {
                            val semanaDto = com.example.forcetrack.network.dto.SemanaDto(
                                id = 0,
                                bloqueId = bloqueCreado.id,
                                numeroSemana = i,
                                dias = null
                            )

                            remoteRepository.createSemana(semanaDto)
                                .onSuccess { semanaCreada ->
                                    Log.d("BloquesViewModel", "Semana $i subida a Xano con ID: ${semanaCreada.id}")

                                    // Crear días en Xano
                                    val nombresDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

                                    for (d in 1..diasPorSemana) {
                                        val nombreDia = if (d <= nombresDias.size) nombresDias[d - 1] else "Día $d"

                                        val diaDto = com.example.forcetrack.network.dto.DiaDto(
                                            id = 0,
                                            semanaId = semanaCreada.id,
                                            nombre = nombreDia,
                                            notas = null,
                                            ejercicios = null
                                        )

                                        remoteRepository.createDia(diaDto)
                                            .onSuccess {
                                                Log.d("BloquesViewModel", "Día $nombreDia subido a Xano")
                                            }
                                            .onFailure { error ->
                                                Log.w("BloquesViewModel", "Error subiendo día a Xano: ${error.message}")
                                            }
                                    }
                                }
                                .onFailure { error ->
                                    Log.w("BloquesViewModel", "Error subiendo semana a Xano: ${error.message}")
                                }
                        }

                        Log.d("BloquesViewModel", "Bloque sincronizado completamente con Xano")
                    }
                    .onFailure { error ->
                        Log.w("BloquesViewModel", "Error subiendo bloque a Xano: ${error.message}")
                        // No mostrar error al usuario, el bloque ya está guardado localmente
                    }

            } catch (e: Exception) {
                Log.w("BloquesViewModel", "Error en sincronización con Xano: ${e.message}")
                // No afecta la experiencia del usuario
            }
        }
    }

    // Elimina un bloque LOCALMENTE y de Xano
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

                // 2. Eliminar de Xano EN SEGUNDO PLANO
                eliminarBloqueDeXano(bloqueId)

            } catch (e: Exception) {
                Log.e("BloquesViewModel", "Error eliminando bloque: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar: ${e.message}"
                )
                liberarOperacion("eliminar_$bloqueId")
            }
        }
    }

    // Elimina el bloque de Xano en segundo plano
    private fun eliminarBloqueDeXano(bloqueId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BloquesViewModel", "Eliminando bloque $bloqueId de Xano...")

                remoteRepository.deleteBloque(bloqueId)
                    .onSuccess {
                        Log.d("BloquesViewModel", "Bloque eliminado de Xano")
                    }
                    .onFailure { error ->
                        Log.w("BloquesViewModel", "Error eliminando de Xano: ${error.message}")
                        // No afecta al usuario, ya está eliminado localmente
                    }
            } catch (e: Exception) {
                Log.w("BloquesViewModel", "Error en eliminación de Xano: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}