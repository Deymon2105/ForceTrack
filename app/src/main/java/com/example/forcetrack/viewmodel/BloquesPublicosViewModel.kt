package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.network.RetrofitClient
import com.example.forcetrack.network.api.BloquePublicoDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar bloques públicos (comunidad)
 */
class BloquesPublicosViewModel : ViewModel() {

    private val _bloquesPublicos = MutableStateFlow<List<BloquePublicoDto>>(emptyList())
    val bloquesPublicos: StateFlow<List<BloquePublicoDto>> = _bloquesPublicos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    init {
        cargarBloquesPublicos()
    }

    /**
     * Cargar bloques públicos del backend
     */
    fun cargarBloquesPublicos(categoria: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _categoriaSeleccionada.value = categoria

            try {
                val response = RetrofitClient.bloqueApi.getBloquesPublicos(categoria)

                if (response.isSuccessful) {
                    _bloquesPublicos.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar bloques públicos: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filtrar bloques por categoría
     */
    fun filtrarPorCategoria(categoria: String?) {
        cargarBloquesPublicos(categoria)
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }
}
