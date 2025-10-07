package com.example.forcetrack.model

data class EjercicioRutina(
    val nombre: String,
    var reps: Int,
    var peso: Double,
    var rir: Int,
    var descanso: Int // segundos
)
