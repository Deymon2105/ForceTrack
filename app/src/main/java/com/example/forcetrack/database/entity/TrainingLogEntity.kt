package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

// Registro de entrenamiento diario
@Entity(tableName = "training_logs")
data class TrainingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val dateIso: String, // almacenamos la fecha en ISO (yyyy-MM-dd) para facilitar consultas
    val notas: String? = null
)

