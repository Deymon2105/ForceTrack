package com.example.forcetrack.network.test

import android.util.Log
import com.example.forcetrack.network.repository.XanoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase de prueba para verificar la conexión con Xano
 * Usa esto para probar que todos los endpoints funcionen correctamente
 */
object XanoTester {

    private const val TAG = "XanoTester"
    private val repository = XanoRepository()

    /**
     * Prueba completa de todos los endpoints
     * Llama a esta función para verificar que todo funciona
     */
    fun testearTodo(usuarioId: Int = 1) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "Iniciando pruebas de Xano...")

            // Test 1: Login
            testLogin()
            
            // Test 2: Register (comentado para no crear usuarios duplicados)
            // testRegister()
            
            // Test 3: Bloques
            testBloques(usuarioId)
            
            // Test 4: Ejercicios Disponibles
            testEjerciciosDisponibles()
            
            Log.d(TAG, "Pruebas completadas")
        }
    }

    /**
     * Test 1: Login
     */
    fun testLogin() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n========== TEST LOGIN ==========")
            
            repository.login("test@forcetrack.com", "123456")
                .onSuccess { usuario ->
                    Log.d(TAG, "Login EXITOSO")
                    Log.d(TAG, "   Usuario: ${usuario.nombreUsuario}")
                    Log.d(TAG, "   ID: ${usuario.id}")
                    Log.d(TAG, "   Email: ${usuario.correo}")
                    Log.d(TAG, "   Creado: ${usuario.created_at}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Login FALLÓ: ${error.message}")
                    error.printStackTrace()
                }
        }
    }

    /**
     * Test 2: Register (Descomenta para probar)
     */
    fun testRegister() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n========== TEST REGISTER ==========")
            
            val nuevoUsuario = "user_${System.currentTimeMillis()}"
            
            repository.register(
                nombreUsuario = nuevoUsuario,
                correo = "$nuevoUsuario@test.com",
                contrasena = "password123"
            )
                .onSuccess { usuario ->
                    Log.d(TAG, "Registro EXITOSO")
                    Log.d(TAG, "   Usuario: ${usuario.nombreUsuario}")
                    Log.d(TAG, "   ID: ${usuario.id}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Registro FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 3: Bloques (Obtener y Crear)
     */
    fun testBloques(usuarioId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n========== TEST BLOQUES ==========")
            
            // Obtener bloques existentes
            repository.obtenerBloques(usuarioId)
                .onSuccess { bloques ->
                    Log.d(TAG, "Obtener bloques EXITOSO")
                    Log.d(TAG, "   Total bloques: ${bloques.size}")
                    bloques.forEach { bloque ->
                        Log.d(TAG, "   - ID ${bloque.id}: ${bloque.nombre}")
                    }
                    
                    // Crear un nuevo bloque
                    testCrearBloque(usuarioId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Obtener bloques FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 3b: Crear Bloque
     */
    private fun testCrearBloque(usuarioId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST CREAR BLOQUE ---")
            
            val nombreBloque = "Bloque Test ${System.currentTimeMillis()}"
            
            repository.crearBloque(usuarioId, nombreBloque)
                .onSuccess { bloque ->
                    Log.d(TAG, "Crear bloque EXITOSO")
                    Log.d(TAG, "   Nombre: ${bloque.nombre}")
                    Log.d(TAG, "   ID: ${bloque.id}")
                    
                    // Test: Crear semana para este bloque
                    // testCrearSemana(bloque.id)
                }
                .onFailure { error ->
                    Log.e(TAG, "Crear bloque FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 4: Crear Semana
     */
    /*
    private fun testCrearSemana(bloqueId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST CREAR SEMANA ---")
            
            repository.crearSemana(bloqueId, 1)
                .onSuccess { semana ->
                    Log.d(TAG, "Crear semana EXITOSA")
                    Log.d(TAG, "   Bloque ID: ${semana.bloqueId}")
                    Log.d(TAG, "   Número: ${semana.numeroSemana}")
                    Log.d(TAG, "   ID: ${semana.id}")
                    
                    // Test: Crear día para esta semana
                    testCrearDia(semana.id)
                }
                .onFailure { error ->
                    Log.e(TAG, "Crear semana FALLÓ: ${error.message}")
                }
        }
    }
    */

    /**
     * Test 5: Crear Día
     */
    /*
    private fun testCrearDia(semanaId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST CREAR DÍA ---")
            
            repository.crearDia(semanaId, "Día Push", "Pecho, Hombros, Tríceps")
                .onSuccess { dia ->
                    Log.d(TAG, "Crear día EXITOSO")
                    Log.d(TAG, "   Nombre: ${dia.nombre}")
                    Log.d(TAG, "   Notas: ${dia.notas}")
                    Log.d(TAG, "   ID: ${dia.id}")
                    
                    // Test: Crear ejercicio para este día
                    testCrearEjercicio(dia.id)
                }
                .onFailure { error ->
                    Log.e(TAG, "Crear día FALLÓ: ${error.message}")
                }
        }
    }
    */

    /**
     * Test 6: Crear Ejercicio
     */
    private fun testCrearEjercicio(diaId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST CREAR EJERCICIO ---")
            
            repository.crearEjercicio(diaId, "Press Banca", 120)
                .onSuccess { ejercicio ->
                    Log.d(TAG, "Crear ejercicio EXITOSO")
                    Log.d(TAG, "   Nombre: ${ejercicio.nombre}")
                    Log.d(TAG, "   Descanso: ${ejercicio.descansoSegundos}s")
                    Log.d(TAG, "   ID: ${ejercicio.id}")
                    
                    // Test: Crear serie para este ejercicio
                    testCrearSerie(ejercicio.id)
                }
                .onFailure { error ->
                    Log.e(TAG, "Crear ejercicio FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 7: Crear y Actualizar Serie
     */
    private fun testCrearSerie(ejercicioId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST CREAR SERIE ---")
            
            repository.crearSerie(
                ejercicioId = ejercicioId,
                peso = 60.0,
                repeticiones = 10,
                rir = 2,
                completada = false
            )
                .onSuccess { serie ->
                    Log.d(TAG, "Crear serie EXITOSA")
                    Log.d(TAG, "   Peso: ${serie.peso} kg")
                    Log.d(TAG, "   Reps: ${serie.repeticiones}")
                    Log.d(TAG, "   RIR: ${serie.rir}")
                    Log.d(TAG, "   ID: ${serie.id}")
                    
                    // Test: Actualizar la serie
                    testActualizarSerie(serie.id)
                }
                .onFailure { error ->
                    Log.e(TAG, "Crear serie FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 8: Actualizar Serie
     */
    private fun testActualizarSerie(serieId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST ACTUALIZAR SERIE ---")
            
            repository.actualizarSerie(
                serieId = serieId,
                peso = 65.0,
                repeticiones = 12,
                completada = true
            )
                .onSuccess { serie ->
                    Log.d(TAG, "Actualizar serie EXITOSA")
                    Log.d(TAG, "   Peso actualizado: ${serie.peso} kg")
                    Log.d(TAG, "   Reps actualizadas: ${serie.repeticiones}")
                    Log.d(TAG, "   Completada: ${serie.completada}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Actualizar serie FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 9: Ejercicios Disponibles
     */
    fun testEjerciciosDisponibles() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n========== TEST EJERCICIOS DISPONIBLES ==========")

            // Obtener todos
            repository.obtenerEjerciciosDisponibles()
                .onSuccess { ejercicios ->
                    Log.d(TAG, "Obtener ejercicios disponibles EXITOSO")
                    Log.d(TAG, "   Total ejercicios: ${ejercicios.size}")

                    // Agrupar por tipo
                    val porTipo = ejercicios.groupBy { it.tipo }
                    porTipo.forEach { (tipo, lista) ->
                        Log.d(TAG, "   $tipo: ${lista.size} ejercicios")
                    }

                    // Test: Obtener solo de un tipo
                    testEjerciciosPorTipo("Pecho")
                }
                .onFailure { error ->
                    Log.e(TAG, "Obtener ejercicios FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test 10: Ejercicios por Tipo
     */
    private fun testEjerciciosPorTipo(tipo: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\n--- TEST EJERCICIOS POR TIPO: $tipo ---")

            repository.obtenerEjerciciosPorTipo(tipo)
                .onSuccess { ejercicios ->
                    Log.d(TAG, "Obtener ejercicios de $tipo EXITOSO")
                    Log.d(TAG, "   Total: ${ejercicios.size}")
                    ejercicios.take(5).forEach { ejercicio ->
                        Log.d(TAG, "   - ${ejercicio.nombre}")
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Obtener ejercicios por tipo FALLÓ: ${error.message}")
                }
        }
    }

    /**
     * Test rápido de conexión (no requiere usuarios existentes)
     */
    fun testRapido() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "Test rápido de conexión a Xano...")

            // Test 1: Verificar que podemos conectarnos a Xano
            try {
                Log.d(TAG, "Intentando conectar a Xano...")

                // Intentar login con el usuario de prueba
                repository.login("test@forcetrack.com", "123456")
                    .onSuccess { usuario ->
                        Log.d(TAG, "CONEXIÓN A XANO EXITOSA!")
                        Log.d(TAG, "   Usuario encontrado: ${usuario.nombreUsuario}")
                        Log.d(TAG, "   ID: ${usuario.id}")
                        Log.d(TAG, "   Correo: ${usuario.correo}")
                    }
                    .onFailure { error ->
                        Log.w(TAG, "No hay usuarios o credenciales incorrectas")
                        Log.d(TAG, "   Probando crear un usuario de prueba...")

                        // Intentar crear un usuario de prueba
                        testCrearPrimerUsuario()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "ERROR DE CONEXIÓN A XANO: ${e.message}")
                Log.e(TAG, "   Verifica:")
                Log.e(TAG, "   1. Las URLs en XanoConfig.kt")
                Log.e(TAG, "   2. Tu conexión a Internet")
                Log.e(TAG, "   3. Que los endpoints de Xano estén activos")
                e.printStackTrace()
            }
        }
    }

    /**
     * Crear primer usuario de prueba si no existe ninguno
     */
    private fun testCrearPrimerUsuario() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "\nCreando usuario de prueba en Xano...")

            repository.register(
                nombreUsuario = "usuario_test",
                correo = "test@forcetrack.com",
                contrasena = "123456"
            )
                .onSuccess { usuario ->
                    Log.d(TAG, "USUARIO DE PRUEBA CREADO EXITOSAMENTE!")
                    Log.d(TAG, "   Usuario: ${usuario.nombreUsuario}")
                    Log.d(TAG, "   Correo: ${usuario.correo}")
                    Log.d(TAG, "   ID: ${usuario.id}")
                    Log.d(TAG, "")
                    Log.d(TAG, "AHORA PUEDES USAR ESTAS CREDENCIALES:")
                    Log.d(TAG, "   Correo: test@forcetrack.com")
                    Log.d(TAG, "   Contraseña: 123456")
                }
                .onFailure { error ->
                    Log.e(TAG, "Error creando usuario: ${error.message}")
                    Log.e(TAG, "   Verifica que los endpoints de Xano estén correctamente configurados")
                    error.printStackTrace()
                }
        }
    }

    /**
     * Test de obtener datos existentes (no crea nada nuevo)
     */
    fun testSoloLectura(usuarioId: Int = 1) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "Test de solo lectura...")

            // Login
            testLogin()

            // Obtener bloques
            repository.obtenerBloques(usuarioId)
                .onSuccess { bloques ->
                    Log.d(TAG, "✅ Bloques: ${bloques.size}")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error bloques: ${error.message}")
                }

            // Obtener ejercicios disponibles
            repository.obtenerEjerciciosDisponibles()
                .onSuccess { ejercicios ->
                    Log.d(TAG, "✅ Ejercicios disponibles: ${ejercicios.size}")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error ejercicios: ${error.message}")
                }
        }
    }
}
