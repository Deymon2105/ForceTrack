package com.example.forcetrack.model

// Modelo simple de Usuario para usar en la UI
data class Usuario(
    val id: Int = 0,
    val nombreUsuario: String,
    val correo: String,
    val contrasena: String
)
