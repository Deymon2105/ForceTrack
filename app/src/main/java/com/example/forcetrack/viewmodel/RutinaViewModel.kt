package com.example.forcetrack.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.model.EjercicioRutina

class RutinaViewModel: ViewModel() {
    val diaRutina = mutableStateOf<DiaRutina?>(null)

    fun setDiaRutina(dia: DiaRutina) {
        diaRutina.value = dia
    }

    fun agregarEjercicio(ejercicio: EjercicioRutina) {
        diaRutina.value?.ejercicios?.add(ejercicio)
    }

    fun eliminarEjercicio(index: Int) {
        diaRutina.value?.ejercicios?.removeAt(index)
    }

    fun actualizarNota(nota: String) {
        diaRutina.value?.notas = nota
    }
}
