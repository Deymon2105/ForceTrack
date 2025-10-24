package com.example.forcetrack.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.forcetrack.database.dao.*
import com.example.forcetrack.database.entity.*

// Esta es la clase principal de la base de datos de la aplicación.
// Define la configuración de la base de datos y sirve como el punto de acceso principal a los datos.
@Database(
    // Lista de todas las "tablas" (entidades) que formarán parte de la base de datos.
    entities = [
        UsuarioEntity::class,
        BloqueEntity::class,
        SemanaEntity::class,
        DiaEntity::class,
        EjercicioEntity::class,
        SerieEntity::class,
        TrainingLogEntity::class, // nueva entidad de logs diarios
        EjercicioDisponibleEntity::class // nueva entidad de ejercicios disponibles
    ],
    version = 4, // Incrementada la versión por la nueva entidad e índice único
    exportSchema = false // No exportar el esquema de la BD a un archivo. Para este proyecto no es necesario.
)
@TypeConverters(Conversores::class)
abstract class AppDatabase : RoomDatabase() {

    // Métodos abstractos para que Room nos proporcione una instancia de cada DAO.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun bloqueDao(): BloqueDao
    abstract fun semanaDao(): SemanaDao
    abstract fun diaDao(): DiaDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun serieDao(): SerieDao
    abstract fun trainingLogDao(): TrainingLogDao // nuevo DAO
    abstract fun ejercicioDisponibleDao(): EjercicioDisponibleDao // nuevo DAO

    // El "companion object" nos permite acceder a los métodos para crear o obtener la base de datos
    // sin necesidad de instanciar la clase AppDatabase.
    companion object {

        // La anotación @Volatile asegura que el valor de INSTANCE siempre esté actualizado
        // y sea el mismo para todos los hilos de ejecución. Es clave para evitar problemas
        // de concurrencia al acceder a la base de datos desde diferentes partes de la app.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Este método nos da la instancia única (singleton) de la base de datos.
        // Si la instancia ya existe, la devuelve. Si no, la crea de forma segura.
        fun getDatabase(context: Context): AppDatabase {
            // El `synchronized` bloquea el acceso a este bloque de código desde múltiples hilos
            // a la vez, asegurando que solo un hilo pueda crear la instancia de la base de datos.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "forcetrack_database" // El nombre del archivo de la base de datos en el dispositivo.
                )
                .fallbackToDestructiveMigration() // Evita errores de migración en desarrollo
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
