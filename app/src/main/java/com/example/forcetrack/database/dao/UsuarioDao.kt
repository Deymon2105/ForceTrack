package com.example.forcetrack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forcetrack.database.entity.UsuarioEntity

// DAO para la tabla de Usuarios.
// Define las operaciones de base de datos para la tabla "usuarios".
@Dao
interface UsuarioDao {

    // Inserta un nuevo usuario en la base de datos.
    // Devuelve el ID del usuario insertado.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    // Busca un usuario por su nombre de usuario.
    // Útil para comprobar si un nombre de usuario ya existe.
    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario LIMIT 1")
    suspend fun obtenerUsuarioPorNombre(nombreUsuario: String): UsuarioEntity?

    // Busca un usuario por su correo electrónico.
    // Útil para la función de inicio de sesión.
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    // Busca un usuario por su ID.
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId LIMIT 1")
    suspend fun obtenerUsuarioPorId(usuarioId: Int): UsuarioEntity?
}
