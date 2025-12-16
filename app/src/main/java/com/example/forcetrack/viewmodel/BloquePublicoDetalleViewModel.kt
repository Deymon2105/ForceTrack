package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.network.RetrofitClient
import com.example.forcetrack.network.dto.BloquePublicoDto
import com.example.forcetrack.network.dto.DiaDto
import com.example.forcetrack.network.dto.EjercicioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para ver el detalle de un bloque público (solo lectura)
 */
class BloquePublicoDetalleViewModel : ViewModel() {

    private val _bloque = MutableStateFlow<BloquePublicoDto?>(null)
    val bloque: StateFlow<BloquePublicoDto?> = _bloque.asStateFlow()

    private val _dias = MutableStateFlow<List<DiaConEjercicios>>(emptyList())
    val dias: StateFlow<List<DiaConEjercicios>> = _dias.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Clase auxiliar para agrupar días con sus ejercicios
     */
    data class DiaConEjercicios(
        val dia: DiaDto,
        val ejercicios: List<EjercicioDto>
    )

    /**
     * Cargar información del bloque público
     */
    fun cargarBloque(bloqueId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Obtener lista de bloques públicos y buscar el específico
                val bloquesResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.bloqueApi.getBloquesPublicos()
                }

                if (bloquesResponse.isSuccessful) {
                    val bloqueEncontrado = bloquesResponse.body()?.find { it.id == bloqueId }
                    
                    if (bloqueEncontrado != null) {
                        _bloque.value = bloqueEncontrado
                        
                        // Cargar días del bloque
                        cargarDias(bloqueId)
                    } else {
                        _error.value = "Bloque no encontrado"
                    }
                } else {
                    _error.value = "Error al cargar el bloque: ${bloquesResponse.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar los días del bloque con sus ejercicios
     */
    private suspend fun cargarDias(bloqueId: Int) {
        try {
            val diasResponse = withContext(Dispatchers.IO) {
                RetrofitClient.diaApi.getDiasByBloque(bloqueId)
            }

            if (diasResponse.isSuccessful) {
                val diasDto = diasResponse.body()?.sortedBy { it.id } ?: emptyList()
                
                // Cargar ejercicios para cada día
                val diasConEjercicios = diasDto.map { dia ->
                    val ejercicios = cargarEjerciciosDia(dia.id)
                    DiaConEjercicios(dia, ejercicios)
                }
                
                _dias.value = diasConEjercicios
            }
        } catch (e: Exception) {
            // Los días son opcionales, no mostrar error si fallan
        }
    }
    
    /**
     * Cargar ejercicios de un día específico
     */
    private suspend fun cargarEjerciciosDia(diaId: Int): List<EjercicioDto> {
        return try {
            val ejerciciosResponse = withContext(Dispatchers.IO) {
                RetrofitClient.ejercicioApi.getEjerciciosByDia(diaId)
            }
            
            if (ejerciciosResponse.isSuccessful) {
                ejerciciosResponse.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
