package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Representa la tabla "bloques" en la base de datos.
// Un bloque es un período de entrenamiento planificado, por ejemplo, "Bloque de Hipertrofia 1".
@Entity(
    tableName = "bloques",
    // Clave foránea para vincular este bloque a un usuario.
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,   // La tabla padre es "usuarios".
            parentColumns = ["id"],        // Columna de referencia en la tabla padre.
            childColumns = ["usuarioId"],  // Columna de esta tabla que establece el vínculo.
            onDelete = ForeignKey.CASCADE   // Si se borra un usuario, se borran sus bloques.
        )
    ],
    // Índice en `usuarioId` para acelerar las consultas que buscan bloques por usuario.
    indices = [Index(value = ["usuarioId"])]
)
data class BloqueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // ID del usuario al que pertenece este bloque de entrenamiento.
    val usuarioId: Int,
    // Nombre del bloque, por ejemplo, "Volumen Fase 1" o "Definición 2024".
    val nombre: String
)
