package com.example.forcetrack.repository

import com.example.forcetrack.model.*

object MockRepository {
    val ejerciciosDisponibles = listOf(
        EjercicioDisponible("Gym", "Press Banca"),
        EjercicioDisponible("Gym", "Remo con Barra"),
        EjercicioDisponible("Gym", "Peso Muerto"),
        EjercicioDisponible("Gym", "Sentadilla"),
        EjercicioDisponible("Gym", "Hack"),
        EjercicioDisponible("Gym", "Elevaciones Laterales"),
        EjercicioDisponible("Calistenia", "Dominados"),
        EjercicioDisponible("Calistenia", "Flexiones"),
        EjercicioDisponible("Calistenia", "Elevaciones de Piernas"),
        EjercicioDisponible("Calistenia", "Fondos"),
        EjercicioDisponible("Calistenia", "Pike Push Ups"),
        EjercicioDisponible("Calistenia", "Crunches"),
        EjercicioDisponible("Cardio", "Cuerda"),
        EjercicioDisponible("Cardio", "Escalera"),
        EjercicioDisponible("Cardio", "Trotar"),
        EjercicioDisponible("Cardio", "Elíptica"),
        EjercicioDisponible("Cardio", "Bicicleta")
    )

    var usuarios = mutableListOf<Usuario>()
    var bloques = mutableListOf<BloqueEntrenamiento>()

    fun registrarUsuario(usuario: Usuario) {
        usuarios.add(usuario)
    }

    fun autenticar(email: String, password: String): Boolean {
        return usuarios.any { it.email == email && it.password == password }
    }

    fun agregarBloque(bloque: BloqueEntrenamiento) {
        bloques.add(bloque)
    }

    fun obtenerBloques(): List<BloqueEntrenamiento> = bloques
}
