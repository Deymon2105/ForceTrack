package com.example.forcetrack.model

// Representa un ejercicio que el usuario puede elegir de una lista predefinida.
// Es un objeto simple para mostrar en la interfaz de usuario.
data class EjercicioDisponible(
    val tipo: String, // Por ejemplo: "Gimnasio", "Calistenia", "Cardio"
    val nombre: String
)
