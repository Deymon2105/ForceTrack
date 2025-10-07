package com.example.forcetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.example.forcetrack.ui.*
import com.example.forcetrack.viewmodel.*

@Composable
fun AppNavigation() {
    val authViewModel = remember { AuthViewModel() }
    val bloquesViewModel = remember { BloquesViewModel() }
    val rutinaViewModel = remember { RutinaViewModel() }

    var screen by remember { mutableStateOf("login") }
    var bloqueSelected: com.example.forcetrack.model.BloqueEntrenamiento? by remember { mutableStateOf(null) }
    var diaSelected: com.example.forcetrack.model.DiaRutina? by remember { mutableStateOf(null) }

    when(screen) {
        "login" -> LoginScreen(authViewModel,
            onLoginSuccess = { screen = "bloques" },
            onRegisterClick = { screen = "registro" }
        )
        "registro" -> RegistroScreen(authViewModel,
            onRegisterSuccess = { screen = "login" }
        )
        "bloques" -> BloquesScreen(bloquesViewModel,
            onBloqueSelected = {
                bloqueSelected = it
                screen = "split"
            }
        )
        "split" -> bloqueSelected?.let { bloque ->
            SplitSemanalScreen(bloque,
                onDiaSelected = {
                    rutinaViewModel.setDiaRutina(it)
                    diaSelected = it
                    screen = "rutina"
                }
            )
        }
        "rutina" -> RutinaDiariaScreen(rutinaViewModel,
            onAgregarEjercicio = { screen = "ejercicios" }
        )
        "ejercicios" -> EjerciciosScreen(
            onEjercicioAdd = { ejercicioDisponible ->
                val ejercicioRutina = com.example.forcetrack.model.EjercicioRutina(
                    nombre = ejercicioDisponible.nombre,
                    reps = 0,
                    peso = 0.0,
                    rir = 0,
                    descanso = 0
                )
                rutinaViewModel.agregarEjercicio(ejercicioRutina)
                screen = "rutina"
            }
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}
