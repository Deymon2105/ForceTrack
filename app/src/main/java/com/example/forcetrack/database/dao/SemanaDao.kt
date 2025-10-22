package com.example.forcetrack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forcetrack.database.entity.SemanaEntity
import kotlinx.coroutines.flow.Flow

// DAO para la tabla de Semanas.
// Define las operaciones de base de datos para la tabla "semanas".
@Dao
interface SemanaDao {

    // Inserta una nueva semana en la base de datos.
    // Devuelve el ID de la semana insertada.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSemana(semana: SemanaEntity): Long

    // Obtiene todas las semanas de un bloque de entrenamiento específico, ordenadas por su número.
    // Usa Flow para que la UI se actualice si los datos cambian.
    @Query("SELECT * FROM semanas WHERE bloqueId = :bloqueId ORDER BY numeroSemana ASC")
    fun obtenerSemanasPorBloque(bloqueId: Int): Flow<List<SemanaEntity>>

    // Elimina una semana usando su ID.
    @Query("DELETE FROM semanas WHERE id = :semanaId")
    suspend fun eliminarSemana(semanaId: Int)
}
