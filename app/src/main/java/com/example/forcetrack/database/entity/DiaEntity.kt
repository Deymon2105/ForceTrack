package com.example.forcetrack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dias",
    foreignKeys = [
        ForeignKey(
            entity = BloqueEntity::class,
            parentColumns = ["id"],
            childColumns = ["bloqueId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bloqueId"])]
)
data class DiaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bloqueId: Int,
    val nombre: String,
    val notas: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val numeroSemana: Int = 1
)
