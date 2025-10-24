package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ejercicios_disponibles",
    indices = [Index(value = ["tipo", "nombre"], unique = true)]
)
data class EjercicioDisponibleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    val nombre: String
)
