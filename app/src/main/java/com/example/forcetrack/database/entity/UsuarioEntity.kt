package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla de usuarios en la base de datos
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreUsuario: String,
    val correo: String,
    val contrasena: String
)

