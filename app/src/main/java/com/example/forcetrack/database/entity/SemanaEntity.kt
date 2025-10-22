package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Representa la tabla "semanas" en la base de datos.
// Cada fila es una semana dentro de un bloque de entrenamiento.
@Entity(
    tableName = "semanas",
    // Clave foránea para vincular esta semana a un bloque.
    foreignKeys = [
        ForeignKey(
            entity = BloqueEntity::class,    // La tabla padre es "bloques".
            parentColumns = ["id"],       // Columna de referencia en la tabla padre.
            childColumns = ["bloqueId"],  // Columna de esta tabla que establece el vínculo.
            onDelete = ForeignKey.CASCADE  // Si se borra un bloque, se borran sus semanas.
        )
    ],
    // Índice en `bloqueId` para acelerar las consultas que buscan semanas por bloque.
    indices = [Index(value = ["bloqueId"])]
)
data class SemanaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // ID del bloque al que pertenece esta semana.
    val bloqueId: Int,
    // El número de la semana dentro del bloque (Semana 1, Semana 2, etc.).
    val numeroSemana: Int
)
