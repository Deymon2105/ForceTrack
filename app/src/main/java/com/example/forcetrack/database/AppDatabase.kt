package com.example.forcetrack.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.forcetrack.database.dao.*
import com.example.forcetrack.database.entity.*

// Esta es la clase principal de la base de datos de la aplicación.
// Define la configuración de la base de datos y sirve como el punto de acceso principal a los datos.
@Database(
    // Lista de todas las "tablas" (entidades) que formarán parte de la base de datos.
    entities = [
        UsuarioEntity::class,
        BloqueEntity::class,
        DiaEntity::class,
        EjercicioEntity::class,
        SerieEntity::class,
        TrainingLogEntity::class, // nueva entidad de logs diarios
        EjercicioDisponibleEntity::class // nueva entidad de ejercicios disponibles
    ],
    version = 8, // Incrementada la versión por los nuevos campos completado y fechaCompletado en DiaEntity
    exportSchema = false // No exportar el esquema de la BD a un archivo. Para este proyecto no es necesario.
)
@TypeConverters(Conversores::class)
abstract class AppDatabase : RoomDatabase() {

    // Métodos abstractos para que Room nos proporcione una instancia de cada DAO.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun bloqueDao(): BloqueDao
    abstract fun diaDao(): DiaDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun serieDao(): SerieDao
    abstract fun trainingLogDao(): TrainingLogDao // nuevo DAO
    abstract fun ejercicioDisponibleDao(): EjercicioDisponibleDao // nuevo DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "forcetrack_database" // El nombre del archivo de la base de datos en el dispositivo.
                )
                .fallbackToDestructiveMigration() // Evita errores de migración en desarrollo
                // ✅ HABILITAR FOREIGN KEYS
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Habilitar foreign keys en cada conexión
                        db.execSQL("PRAGMA foreign_keys=ON;")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
