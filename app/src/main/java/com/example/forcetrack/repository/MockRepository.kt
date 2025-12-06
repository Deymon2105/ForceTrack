package com.example.forcetrack.repository

import com.example.forcetrack.model.*

object MockRepository {
    val ejerciciosDisponibles = listOf(
        // Gym - Pecho
        EjercicioDisponible("Gym - Pecho", "Press Banca"),
        EjercicioDisponible("Gym - Pecho", "Press Inclinado"),
        EjercicioDisponible("Gym - Pecho", "Aperturas con Mancuernas"),
        EjercicioDisponible("Gym - Pecho", "Cruce de Poleas"),
        
        // Gym - Espalda
        EjercicioDisponible("Gym - Espalda", "Remo con Barra"),
        EjercicioDisponible("Gym - Espalda", "Jalón al Pecho"),
        EjercicioDisponible("Gym - Espalda", "Remo Gironda"),
        EjercicioDisponible("Gym - Espalda", "Pull Over"),

        // Gym - Pierna
        EjercicioDisponible("Gym - Pierna", "Sentadilla"),
        EjercicioDisponible("Gym - Pierna", "Prensa"),
        EjercicioDisponible("Gym - Pierna", "Peso Muerto Rumano"),
        EjercicioDisponible("Gym - Pierna", "Extensiones de Cuádriceps"),
        EjercicioDisponible("Gym - Pierna", "Curl Femoral"),
        EjercicioDisponible("Gym - Pierna", "Elevación de Talones"),

        // Gym - Hombro
        EjercicioDisponible("Gym - Hombro", "Press Militar"),
        EjercicioDisponible("Gym - Hombro", "Elevaciones Laterales"),
        EjercicioDisponible("Gym - Hombro", "Pájaros"),

        // Gym - Brazos
        EjercicioDisponible("Gym - Brazos", "Curl de Bíceps con Barra"),
        EjercicioDisponible("Gym - Brazos", "Martillo"),
        EjercicioDisponible("Gym - Brazos", "Extensiones de Tríceps"),
        EjercicioDisponible("Gym - Brazos", "Press Francés"),

        // Calistenia
        EjercicioDisponible("Calistenia", "Dominadas"),
        EjercicioDisponible("Calistenia", "Flexiones"),
        EjercicioDisponible("Calistenia", "Fondos en Paralelas"),
        EjercicioDisponible("Calistenia", "Muscle Up"),
        EjercicioDisponible("Calistenia", "Front Lever"),
        EjercicioDisponible("Calistenia", "Pistol Squat"),
        EjercicioDisponible("Calistenia", "Handstand Push Up"),
        EjercicioDisponible("Calistenia", "Australian Pull Ups"),

        // Boxeo / MMA
        EjercicioDisponible("Boxeo / MMA", "Sombra"),
        EjercicioDisponible("Boxeo / MMA", "Saco Pesado"),
        EjercicioDisponible("Boxeo / MMA", "Pera de Velocidad"),
        EjercicioDisponible("Boxeo / MMA", "Sparring"),
        EjercicioDisponible("Boxeo / MMA", "Saltar la Cuerda"),
        EjercicioDisponible("Boxeo / MMA", "Grappling"),
        EjercicioDisponible("Boxeo / MMA", "Patadas al Saco"),

        // Yoga / Estiramiento
        EjercicioDisponible("Yoga / Flexibilidad", "Saludo al Sol"),
        EjercicioDisponible("Yoga / Flexibilidad", "Perro Boca Abajo"),
        EjercicioDisponible("Yoga / Flexibilidad", "Postura del Guerrero"),
        EjercicioDisponible("Yoga / Flexibilidad", "Estiramiento de Isquios"),
        EjercicioDisponible("Yoga / Flexibilidad", "Estiramiento de Pectoral"),
        EjercicioDisponible("Yoga / Flexibilidad", "Foam Rolling"),

        // Cardio
        EjercicioDisponible("Cardio", "Cinta de Correr"),
        EjercicioDisponible("Cardio", "Elíptica"),
        EjercicioDisponible("Cardio", "Bicicleta Estática"),
        EjercicioDisponible("Cardio", "Remo"),
        EjercicioDisponible("Cardio", "Escaladora")
    )

    var usuarios = mutableListOf<Usuario>()
    // Iniciar sin bloques predefinidos
    var bloques = mutableListOf<BloqueEntrenamiento>()

    fun registrarUsuario(usuario: Usuario) {
        usuarios.add(usuario)
    }

    fun autenticar(email: String, password: String): Boolean {
        return usuarios.any { it.correo == email && it.contrasena == password }
    }

    fun agregarBloque(bloque: BloqueEntrenamiento) {
        bloques.add(bloque)
    }

    fun eliminarBloque(bloqueId: Int) {
        bloques.removeAll { it.id == bloqueId }
    }

    fun obtenerBloques(): List<BloqueEntrenamiento> = bloques
}

