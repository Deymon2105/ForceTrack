package com.example.forcetrack.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.forcetrack.model.BloqueEntrenamiento
import com.example.forcetrack.repository.MockRepository

class BloquesViewModel: ViewModel() {
    var bloques = mutableStateListOf<BloqueEntrenamiento>().apply {
        addAll(MockRepository.obtenerBloques())
    }

    fun agregarBloque(bloque: BloqueEntrenamiento) {
        MockRepository.agregarBloque(bloque)
        bloques.clear()
        bloques.addAll(MockRepository.obtenerBloques())
    }
}
