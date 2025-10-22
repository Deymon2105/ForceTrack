package com.example.forcetrack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.forcetrack.database.entity.EjercicioEntity
import kotlinx.coroutines.flow.Flow

// DAO (Objeto de Acceso a Datos) para la tabla de Ejercicios.
// Define las operaciones de base de datos que se pueden realizar sobre la tabla "ejercicios".
@Dao
interface EjercicioDao {

    // Inserta un nuevo ejercicio en la base de datos.
    // Si un ejercicio con el mismo ID ya existe, lo reemplaza.
    // Devuelve el ID del ejercicio insertado.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEjercicio(ejercicio: EjercicioEntity): Long

    // Obtiene todos los ejercicios que pertenecen a un día específico.
    // Devuelve un Flow, lo que significa que la lista se actualizará automáticamente
    // si los datos cambian en la base de datos.
    @Query("SELECT * FROM ejercicios WHERE diaId = :diaId ORDER BY id ASC")
    fun obtenerEjerciciosPorDia(diaId: Int): Flow<List<EjercicioEntity>>

    // Obtiene un ejercicio específico por su ID.
    @Query("SELECT * FROM ejercicios WHERE id = :ejercicioId")
    suspend fun obtenerEjercicioPorId(ejercicioId: Int): EjercicioEntity?

    // Actualiza los datos de un ejercicio existente.
    @Update
    suspend fun actualizarEjercicio(ejercicio: EjercicioEntity)

    // Elimina un ejercicio de la base de datos usando su ID.
    @Query("DELETE FROM ejercicios WHERE id = :ejercicioId")
    suspend fun eliminarEjercicio(ejercicioId: Int)
}
