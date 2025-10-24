package com.example.forcetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.model.EjercicioDisponible
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EjerciciosViewModel(private val repository: ForceTrackRepository) : ViewModel() {

    private val _ejercicios = MutableStateFlow<List<EjercicioDisponible>>(emptyList())
    val ejercicios: StateFlow<List<EjercicioDisponible>> = _ejercicios.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.obtenerEjerciciosDisponibles().collect { lista ->
                    // mapear la entidad a modelo simple que usa la UI
                    _ejercicios.value = lista.map { EjercicioDisponible(tipo = it.tipo, nombre = it.nombre) }
                }
            } catch (_: Exception) {
                // Evitar que un fallo en la DB rompa la app; exponer lista vacía y podríamos loguear el error
                _ejercicios.value = emptyList()
            }
        }
    }

}
