package com.example.forcetrack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.forcetrack.config.AppRoutes
import com.example.forcetrack.ui.*
import com.example.forcetrack.ui.theme.ForcetrackTheme
import com.example.forcetrack.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ForceTrackApplication
        val repository = app.repository
        val sessionManager = app.sessionManager
        val syncService = app.syncService
        val viewModelFactory = ViewModelFactory(repository, sessionManager, syncService)

        Log.d("MainActivity", "Backend configurado y listo para usar")

        setContent {
            ForcetrackTheme {
                AppNavigation(viewModelFactory = viewModelFactory)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val authState by authViewModel.uiState.collectAsState()

    // Este es el cerebro de la navegación. Reacciona a los cambios de estado de autenticación.
    LaunchedEffect(authState.authState) {
        when (authState.authState) {
            AuthState.LOGGED_IN -> {
                val userId = authState.currentUser?.id ?: return@LaunchedEffect
                navController.navigate(AppRoutes.bloquesWithUserId(userId)) {
                    popUpTo(0) { inclusive = true } // Limpia el stack para que el usuario no pueda volver atrás.
                }
            }
            AuthState.LOGGED_OUT -> {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true } // Limpia el stack al cerrar sesión.
                }
            }
            else -> { /* No se realiza ninguna acción de navegación en los estados LOADING o ERROR */ }
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onRegisterClick = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegistroScreen(
                authViewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.BLOQUES,
            arguments = listOf(navArgument(AppRoutes.BLOQUES_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt(AppRoutes.BLOQUES_ARG) ?: 0
            val bloquesViewModel: BloquesViewModel = viewModel(factory = viewModelFactory)

            LaunchedEffect(usuarioId) {
                if (usuarioId > 0) bloquesViewModel.cargarBloques(usuarioId)
            }
            
            BloquesScreen(
                usuarioId = usuarioId,
                bloquesViewModel = bloquesViewModel,
                onBloqueSelected = { bloqueId -> navController.navigate(AppRoutes.splitWithBloqueId(bloqueId)) },
                onLogout = { authViewModel.logout() },
                onOpenCalendar = { navController.navigate(AppRoutes.calendarWithUserId(usuarioId)) },
                onOpenBloquesPublicos = { navController.navigate(AppRoutes.BLOQUES_PUBLICOS) }
            )
        }

        composable(
            route = AppRoutes.SPLIT,
            arguments = listOf(navArgument(AppRoutes.SPLIT_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val bloqueId = backStackEntry.arguments?.getInt(AppRoutes.SPLIT_ARG) ?: 0
            val splitViewModel: SplitViewModel = viewModel(factory = viewModelFactory)

            LaunchedEffect(bloqueId) {
                if (bloqueId > 0) splitViewModel.loadBlockDetails(bloqueId)
            }

            SplitSemanalScreen(
                splitViewModel = splitViewModel,
                onDiaSelected = { diaId -> navController.navigate(AppRoutes.rutinaWithDiaId(diaId)) },
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.RUTINA,
            arguments = listOf(navArgument(AppRoutes.RUTINA_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getInt(AppRoutes.RUTINA_ARG) ?: 0
            val rutinaViewModel: RutinaViewModel = viewModel(factory = viewModelFactory)

            LaunchedEffect(diaId) {
                if (diaId > 0) rutinaViewModel.cargarDia(diaId)
            }

            RutinaDiariaScreen(
                rutinaViewModel = rutinaViewModel,
                onAgregarEjercicio = { navController.navigate(AppRoutes.ejerciciosWithDiaId(diaId)) },
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.EJERCICIOS,
            arguments = listOf(navArgument(AppRoutes.EJERCICIOS_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getInt(AppRoutes.EJERCICIOS_ARG) ?: 0
            val rutinaRoute = AppRoutes.rutinaWithDiaId(diaId)
            val rutinaBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(rutinaRoute)
            }
            val rutinaViewModel: RutinaViewModel = viewModel(viewModelStoreOwner = rutinaBackStackEntry, factory = viewModelFactory)
            
            // Obtener EjerciciosViewModel para leer ejercicios disponibles persistidos
            val ejerciciosViewModel: EjerciciosViewModel = viewModel(factory = viewModelFactory)
            val ejerciciosList by ejerciciosViewModel.ejercicios.collectAsState()

            EjerciciosScreen(
                ejerciciosDisponibles = ejerciciosList,
                onEjercicioAdd = { ejercicio ->
                    rutinaViewModel.agregarEjercicio(ejercicio.nombre)
                    navController.popBackStack()
                },
                onBackPressed = { navController.popBackStack() }
            )
        }

        // Composable para el calendario (usuarioId como argumento)
        composable(
            route = AppRoutes.CALENDAR,
            arguments = listOf(navArgument(AppRoutes.CALENDAR_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt(AppRoutes.CALENDAR_ARG) ?: 0
            val trainingLogViewModel: TrainingLogViewModel = viewModel(factory = viewModelFactory)

            CalendarScreen(
                usuarioId = usuarioId,
                trainingLogViewModel = trainingLogViewModel,
                onDateSelected = { dateIso ->
                    navController.navigate(AppRoutes.dailyWithParams(usuarioId, dateIso))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Composable para el registro diario (usuarioId y fecha como argumentos)
        composable(
            route = AppRoutes.DAILY,
            arguments = listOf(
                navArgument(AppRoutes.DAILY_ARG_USER) { type = NavType.IntType },
                navArgument(AppRoutes.DAILY_ARG_DATE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getInt(AppRoutes.DAILY_ARG_USER) ?: 0
            val dateIso = backStackEntry.arguments?.getString(AppRoutes.DAILY_ARG_DATE) ?: ""
            val trainingLogViewModel: TrainingLogViewModel = viewModel(factory = viewModelFactory)

            DailyLogScreen(
                usuarioId = usuarioId,
                dateIso = dateIso,
                trainingLogViewModel = trainingLogViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Pantalla de Bloques Públicos (Comunidad)
        composable(AppRoutes.BLOQUES_PUBLICOS) {
            val bloquesPublicosViewModel: BloquesPublicosViewModel = viewModel(factory = viewModelFactory)
            BloquesPublicosScreen(
                navController = navController,
                viewModel = bloquesPublicosViewModel
            )
        }

        // Pantalla de detalle de bloque público (solo lectura)
        composable(
            route = AppRoutes.BLOQUE_PUBLICO_DETALLE,
            arguments = listOf(navArgument(AppRoutes.BLOQUE_PUBLICO_DETALLE_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val bloqueId = backStackEntry.arguments?.getInt(AppRoutes.BLOQUE_PUBLICO_DETALLE_ARG) ?: 0
            BloquePublicoDetalleScreen(
                navController = navController,
                bloqueId = bloqueId
            )
        }
    }
}