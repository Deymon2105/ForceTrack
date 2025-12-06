package com.example.forcetrack

import android.app.Application
import com.example.forcetrack.database.AppDatabase
import com.example.forcetrack.database.repository.ForceTrackRepository
import com.example.forcetrack.database.entity.EjercicioDisponibleEntity
import com.example.forcetrack.repository.MockRepository
import com.example.forcetrack.network.repository.RemoteRepository
import com.example.forcetrack.network.sync.SyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// La clase Application personalizada para nuestra app.
// Se ejecuta antes que cualquier otra pantalla o servicio.
class ForceTrackApplication : Application() {

    // Usamos "lazy" para que la base de datos y el repositorio se creen solo cuando
    // se necesiten por primera vez, no al iniciar la app.
    
    // Se inicializa la base de datos de forma "perezosa" (lazy).
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Se inicializa el repositorio, pasándole todos los DAOs de la base de datos.
    // El repositorio también se crea de forma "perezosa".
    val repository by lazy { 
        ForceTrackRepository(
            database.usuarioDao(),
            database.bloqueDao(),
            database.diaDao(),
            database.ejercicioDao(),
            database.serieDao(),
            database.trainingLogDao(),
            database.ejercicioDisponibleDao()
        )
    }

    // Repositorio remoto para comunicación con Xano
    val remoteRepository by lazy { RemoteRepository() }

    // Servicio de sincronización con Xano
    val syncService by lazy { SyncService() }

    // Exponer SessionManager para persistir la sesión del usuario en el dispositivo
    val sessionManager by lazy { SessionManager(this) }

    override fun onCreate() {
        super.onCreate()
        // Sembrar la tabla de ejercicios disponibles si está vacía
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = repository.countEjerciciosDisponibles()
                if (count == 0) {
                    val lista = MockRepository.ejerciciosDisponibles.map { EjercicioDisponibleEntity(tipo = it.tipo, nombre = it.nombre) }
                    repository.insertarEjerciciosDisponibles(lista)
                }
            } catch (e: Exception) {
                // no bloquear el arranque si falla el seed, registrar si se desea
            }
        }
    }
}
