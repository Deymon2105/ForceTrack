package com.example.forcetrack.model

// Modelos sencillos para la capa UI
// Estos objetos sirven como DTOs para representar la estructura anidada
// (bloque -> semanas -> días) de forma clara y simple.

// Bloque de Entrenamiento usado en la UI
data class BloqueEntrenamiento(
    val id: Int = 0,
    val nombre: String,
    val usuarioId: Int,
    val semanas: MutableList<SemanaEntrenamiento> = mutableListOf()
)

// Semana de entrenamiento usada en la UI
data class SemanaEntrenamiento(
    val id: Int = 0,
    val bloqueId: Int,
    val numero: Int,
    val dias: MutableList<DiaRutina> = mutableListOf()
)

// Día de rutina usado en la UI
data class DiaRutina(
    val id: Int = 0,
    val semanaId: Int,
    val nombre: String,
    val ejercicios: MutableList<EjercicioRutina> = mutableListOf(),
    var notas: String = ""
)
