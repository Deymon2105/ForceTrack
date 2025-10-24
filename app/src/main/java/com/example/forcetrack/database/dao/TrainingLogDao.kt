package com.example.forcetrack.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.forcetrack.database.entity.TrainingLogEntity

@Dao
interface TrainingLogDao {
    @Query("SELECT * FROM training_logs WHERE usuarioId = :userId ORDER BY dateIso")
    fun obtenerLogsPorUsuario(userId: Int): Flow<List<TrainingLogEntity>>

    @Query("SELECT * FROM training_logs WHERE usuarioId = :userId AND dateIso = :dateIso LIMIT 1")
    suspend fun obtenerLogPorFecha(userId: Int, dateIso: String): TrainingLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLog(log: TrainingLogEntity): Long

    @Update
    suspend fun actualizarLog(log: TrainingLogEntity)

    @Query("DELETE FROM training_logs WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Delete
    suspend fun eliminar(log: TrainingLogEntity)
}

