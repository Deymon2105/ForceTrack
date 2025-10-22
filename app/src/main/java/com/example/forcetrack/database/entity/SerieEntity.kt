package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Representa la tabla "series" en la base de datos.
// Cada fila es una serie de un ejercicio específico.
@Entity(
    tableName = "series",
    // Define una clave foránea para asegurar la integridad de los datos.
    // Esto significa que cada serie DEBE estar asociada a un ejercicio.
    foreignKeys = [
        ForeignKey(
            entity = EjercicioEntity::class,      // La tabla padre es "ejercicios".
            parentColumns = ["id"],             // La columna de la tabla padre que se referencia.
            childColumns = ["ejercicioId"],     // La columna de esta tabla que establece el vínculo.
            onDelete = ForeignKey.CASCADE       // Si se borra un ejercicio, todas sus series se borran.
        )
    ]
)
data class SerieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Este campo vincula la serie con un ejercicio.
    // El índice ayuda a que las búsquedas por ejercicioId sean más rápidas.
    val ejercicioId: Int,
    val peso: Double = 0.0,
    val repeticiones: Int = 0,
    val rir: Int = 0, // Repeticiones En Reserva (RIR).
    val completada: Boolean = false
)
