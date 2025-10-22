package com.example.forcetrack.database.dao

import androidx.room.*
import com.example.forcetrack.database.entity.SerieEntity
import kotlinx.coroutines.flow.Flow

// Interface para manejar las series de ejercicios
@Dao
interface SerieDao {

    // Guardar una nueva serie
    @Insert
    suspend fun insertarSerie(serie: SerieEntity): Long

    // Obtener todas las series de un ejercicio
    @Query("SELECT * FROM series WHERE ejercicioId = :ejercicioId")
    fun obtenerSeriesPorEjercicio(ejercicioId: Int): Flow<List<SerieEntity>>

    // Obtener una serie por ID
    @Query("SELECT * FROM series WHERE id = :serieId")
    suspend fun obtenerSeriePorId(serieId: Int): SerieEntity?

    // Actualizar una serie
    @Update
    suspend fun actualizarSerie(serie: SerieEntity)

    // Eliminar una serie
    @Delete
    suspend fun eliminarSerie(serie: SerieEntity)

    // Eliminar series de un ejercicio
    @Query("DELETE FROM series WHERE ejercicioId = :ejercicioId")
    suspend fun eliminarSeriesPorEjercicio(ejercicioId: Int)
}

