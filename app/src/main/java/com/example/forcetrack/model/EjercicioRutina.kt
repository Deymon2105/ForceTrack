package com.example.forcetrack.model

// Modelo sencillo de Serie (usado en UI para representar series dentro de un ejercicio)
data class Serie(
    val id: Int = 0,
    val ejercicioId: Int = 0,
    var peso: Double = 0.0,
    var reps: Int = 0,
    var rir: Int = 0,
    var completada: Boolean = false
)

// Modelo sencillo de Ejercicio (usado en UI para representar ejercicios con sus series)
data class EjercicioRutina(
    val id: Int = 0,
    val diaId: Int = 0,
    val nombre: String,
    val series: MutableList<Serie> = mutableListOf(),
    var descanso: Int = 90 // segundos entre series
)
