package com.example.forcetrack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forcetrack.database.entity.BloqueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloqueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarBloque(bloque: BloqueEntity): Long

    @Query("SELECT * FROM bloques WHERE usuarioId = :usuarioId ORDER BY id ASC")
    fun obtenerBloquesPorUsuario(usuarioId: Int): Flow<List<BloqueEntity>>

    @Query("SELECT * FROM bloques WHERE id = :bloqueId")
    suspend fun obtenerBloquePorId(bloqueId: Int): BloqueEntity?

    @Query("DELETE FROM bloques WHERE id = :bloqueId")
    suspend fun eliminarBloque(bloqueId: Int)
}
