package com.example.forcetrack.database.repository

import com.example.forcetrack.database.dao.*
import com.example.forcetrack.database.entity.*
import kotlinx.coroutines.flow.Flow

class ForceTrackRepository(
    private val usuarioDao: UsuarioDao,
    private val bloqueDao: BloqueDao,
    private val semanaDao: SemanaDao,
    private val diaDao: DiaDao,
    private val ejercicioDao: EjercicioDao,
    private val serieDao: SerieDao
) {

    // --- Operaciones de Usuario ---
    suspend fun iniciarSesion(nombreUsuario: String, contrasena: String): UsuarioEntity? {
        val usuario = usuarioDao.obtenerUsuarioPorNombre(nombreUsuario)
        return if (usuario != null && usuario.contrasena == contrasena) usuario else null
    }

    suspend fun usuarioExiste(nombreUsuario: String): Boolean {
        return usuarioDao.obtenerUsuarioPorNombre(nombreUsuario) != null
    }

    suspend fun registrarUsuario(nombreUsuario: String, correo: String, contrasena: String): Long {
        val nuevoUsuario = UsuarioEntity(nombreUsuario = nombreUsuario, correo = correo, contrasena = contrasena)
        return usuarioDao.insertarUsuario(nuevoUsuario)
    }

    suspend fun obtenerUsuarioPorId(usuarioId: Int): UsuarioEntity? {
        return usuarioDao.obtenerUsuarioPorId(usuarioId)
    }

    // --- Operaciones con Bloques ---
    fun obtenerBloques(usuarioId: Int): Flow<List<BloqueEntity>> {
        return bloqueDao.obtenerBloquesPorUsuario(usuarioId)
    }
    
    suspend fun obtenerBloquePorId(bloqueId: Int): BloqueEntity? {
        return bloqueDao.obtenerBloquePorId(bloqueId)
    }

    suspend fun crearBloque(nombre: String, usuarioId: Int): Long {
        val nuevoBloque = BloqueEntity(nombre = nombre, usuarioId = usuarioId)
        return bloqueDao.insertarBloque(nuevoBloque)
    }

    suspend fun eliminarBloque(bloqueId: Int) {
        bloqueDao.eliminarBloque(bloqueId)
    }

    // --- Operaciones con Semanas ---
    fun obtenerSemanas(bloqueId: Int): Flow<List<SemanaEntity>> {
        return semanaDao.obtenerSemanasPorBloque(bloqueId)
    }

    suspend fun crearSemana(bloqueId: Int, numeroSemana: Int): Long {
        val nuevaSemana = SemanaEntity(bloqueId = bloqueId, numeroSemana = numeroSemana)
        return semanaDao.insertarSemana(nuevaSemana)
    }

    // --- Operaciones con Días ---
    fun obtenerDias(semanaId: Int): Flow<List<DiaEntity>> {
        return diaDao.obtenerDiasPorSemana(semanaId)
    }

    suspend fun obtenerDiaPorId(diaId: Int): DiaEntity? {
        return diaDao.obtenerDiaPorId(diaId)
    }

    suspend fun crearDia(semanaId: Int, nombreDia: String): Long {
        val nuevoDia = DiaEntity(semanaId = semanaId, nombre = nombreDia)
        return diaDao.insertarDia(nuevoDia)
    }

    suspend fun actualizarNotasDia(diaId: Int, notas: String) {
        val dia = diaDao.obtenerDiaPorId(diaId)
        dia?.let { diaDao.actualizarDia(it.copy(notas = notas)) }
    }

    // --- Operaciones con Ejercicios ---
    fun obtenerEjercicios(diaId: Int): Flow<List<EjercicioEntity>> {
        return ejercicioDao.obtenerEjerciciosPorDia(diaId)
    }

    suspend fun agregarEjercicio(diaId: Int, nombreEjercicio: String): Long {
        val nuevoEjercicio = EjercicioEntity(diaId = diaId, nombre = nombreEjercicio)
        return ejercicioDao.insertarEjercicio(nuevoEjercicio)
    }

    suspend fun eliminarEjercicio(ejercicioId: Int) {
        ejercicioDao.eliminarEjercicio(ejercicioId)
    }

    suspend fun actualizarDescansoEjercicio(ejercicioId: Int, descansoSegundos: Int) {
        val ejercicio = ejercicioDao.obtenerEjercicioPorId(ejercicioId)
        ejercicio?.let { ejercicioDao.actualizarEjercicio(it.copy(descansoSegundos = descansoSegundos)) }
    }

    // --- Operaciones con Series ---
    fun obtenerSeries(ejercicioId: Int): Flow<List<SerieEntity>> {
        return serieDao.obtenerSeriesPorEjercicio(ejercicioId)
    }

    suspend fun agregarSerie(ejercicioId: Int): Long {
        val nuevaSerie = SerieEntity(ejercicioId = ejercicioId)
        return serieDao.insertarSerie(nuevaSerie)
    }

    suspend fun eliminarSerie(serieId: Int) {
        val serie = serieDao.obtenerSeriePorId(serieId)
        serie?.let { serieDao.eliminarSerie(it) }
    }

    suspend fun actualizarSerie(serieId: Int, peso: Double, repeticiones: Int, rir: Int, completada: Boolean) {
        val serie = serieDao.obtenerSeriePorId(serieId)
        serie?.let {
            val serieActualizada = it.copy(peso = peso, repeticiones = repeticiones, rir = rir, completada = completada)
            serieDao.actualizarSerie(serieActualizada)
        }
    }
}