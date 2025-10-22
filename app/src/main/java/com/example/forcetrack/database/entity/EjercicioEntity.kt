package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Representa la tabla "ejercicios" en la base de datos.
// Cada fila es un ejercicio dentro de un día de entrenamiento específico.
@Entity(
    tableName = "ejercicios",
    // Se define una clave foránea para vincular cada ejercicio a un día.
    foreignKeys = [
        ForeignKey(
            entity = DiaEntity::class,           // La tabla padre es "dias".
            parentColumns = ["id"],          // La columna de referencia en la tabla padre.
            childColumns = ["diaId"],        // La columna de esta tabla que establece el vínculo.
            onDelete = ForeignKey.CASCADE    // Si se borra un día, todos sus ejercicios se borran.
        )
    ],
    // Se crea un índice en la columna `diaId` para que las consultas que filtran por día
    // (por ejemplo, "dame todos los ejercicios del lunes") sean más rápidas.
    indices = [Index(value = ["diaId"])]
)
data class EjercicioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Este campo vincula el ejercicio con un día.
    val diaId: Int,
    val nombre: String,
    // El tiempo de descanso recomendado para este ejercicio, en segundos.
    val descansoSegundos: Int = 90
)
