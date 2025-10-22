package com.example.forcetrack

import android.app.Application
import com.example.forcetrack.database.AppDatabase
import com.example.forcetrack.database.repository.ForceTrackRepository

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
            database.semanaDao(),
            database.diaDao(),
            database.ejercicioDao(),
            database.serieDao()
        )
    }
}
