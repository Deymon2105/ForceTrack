package com.example.forcetrack.model

data class BloqueEntrenamiento(
    val id: Int,
    val nombre: String,
    val semanas: List<SemanaEntrenamiento>
)

data class SemanaEntrenamiento(
    val numero: Int,
    val dias: List<DiaRutina>
)

data class DiaRutina(
    val nombre: String,
    val ejercicios: MutableList<EjercicioRutina> = mutableListOf(),
    var notas: String = ""
)
