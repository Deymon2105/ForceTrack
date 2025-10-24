package com.example.forcetrack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forcetrack.database.entity.EjercicioDisponibleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EjercicioDisponibleDao {

    @Query("SELECT * FROM ejercicios_disponibles ORDER BY tipo, nombre")
    fun obtenerTodos(): Flow<List<EjercicioDisponibleEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarLista(lista: List<EjercicioDisponibleEntity>)

    @Query("SELECT COUNT(*) FROM ejercicios_disponibles")
    suspend fun count(): Int
}

