package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Representa la tabla "dias" en la base de datos.
// Cada fila es un día de entrenamiento específico dentro de una semana.
@Entity(
    tableName = "dias",
    // Clave foránea para vincular este día a una semana.
    foreignKeys = [
        ForeignKey(
            entity = SemanaEntity::class,    // La tabla padre es "semanas".
            parentColumns = ["id"],       // Columna de referencia en la tabla padre.
            childColumns = ["semanaId"],  // Columna de esta tabla que establece el vínculo.
            onDelete = ForeignKey.CASCADE  // Si se borra una semana, se borran sus días.
        )
    ],
    // Índice en `semanaId` para acelerar las consultas que buscan días por semana.
    indices = [Index(value = ["semanaId"])]
)
data class DiaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // ID de la semana a la que pertenece este día.
    val semanaId: Int,
    val nombre: String, // Por ejemplo: "Día 1", "Lunes", "Pecho y Tríceps"
    val notas: String = "" // Notas opcionales para la sesión de ese día.
)
