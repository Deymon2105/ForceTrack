package com.example.forcetrack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.forcetrack.database.entity.DiaEntity
import kotlinx.coroutines.flow.Flow

// DAO para la tabla de Días.
// Define las operaciones de base de datos para la tabla "dias".
@Dao
interface DiaDao {

    // Inserta un nuevo día en la base de datos.
    // Devuelve el ID del día insertado.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDia(dia: DiaEntity): Long

    // Obtiene todos los días de una semana específica, ordenados por su ID.
    // Usa Flow para que la UI se actualice automáticamente si los datos cambian.
    @Query("SELECT * FROM dias WHERE semanaId = :semanaId ORDER BY id ASC")
    fun obtenerDiasPorSemana(semanaId: Int): Flow<List<DiaEntity>>

    // Obtiene un día específico por su ID.
    @Query("SELECT * FROM dias WHERE id = :diaId")
    suspend fun obtenerDiaPorId(diaId: Int): DiaEntity?

    // Actualiza los datos de un día existente.
    @Update
    suspend fun actualizarDia(dia: DiaEntity)

    // Elimina un día de la base de datos usando su ID.
    @Query("DELETE FROM dias WHERE id = :diaId")
    suspend fun eliminarDia(diaId: Int)
}
