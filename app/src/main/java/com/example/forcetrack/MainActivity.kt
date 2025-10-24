package com.example.forcetrack

import android.os.Bundle
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
import com.example.forcetrack.ui.*
import com.example.forcetrack.ui.theme.ForcetrackTheme
import com.example.forcetrack.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as ForceTrackApplication).repository
        val viewModelFactory = ViewModelFactory(repository)

        setContent {
            ForcetrackTheme {
                AppNavigation(viewModelFactory = viewModelFactory)
            }
        }
    }
}

// Objeto que centraliza las rutas para evitar errores tipográficos y facilitar la navegación
object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val BLOQUES_ROUTE = "bloques"
    const val BLOQUES_ARG = "usuarioId"
    const val BLOQUES = "$BLOQUES_ROUTE/{$BLOQUES_ARG}"

    const val SPLIT_ROUTE = "split"
    const val SPLIT_ARG = "bloqueId"
    const val SPLIT = "$SPLIT_ROUTE/{$SPLIT_ARG}"

    const val RUTINA_ROUTE = "rutina"
    const val RUTINA_ARG = "diaId"
    const val RUTINA = "$RUTINA_ROUTE/{$RUTINA_ARG}"

    const val EJERCICIOS_ROUTE = "ejercicios"
    const val EJERCICIOS_ARG = "diaId"
    const val EJERCICIOS = "$EJERCICIOS_ROUTE/{$EJERCICIOS_ARG}"

    // Nuevas rutas para calendario y registro diario
    const val CALENDAR_ROUTE = "calendar"
    const val CALENDAR_ARG = "usuarioId"
    const val CALENDAR = "$CALENDAR_ROUTE/{$CALENDAR_ARG}"

    const val DAILY_ROUTE = "daily"
    const val DAILY_ARG_USER = "usuarioId"
    const val DAILY_ARG_DATE = "dateIso"
    const val DAILY = "$DAILY_ROUTE/{$DAILY_ARG_USER}/{$DAILY_ARG_DATE}"
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
                navController.navigate("${AppRoutes.BLOQUES_ROUTE}/$userId") {
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
                bloquesViewModel = bloquesViewModel,
                onBloqueSelected = { bloqueId -> navController.navigate("${AppRoutes.SPLIT_ROUTE}/$bloqueId") },
                onLogout = { authViewModel.logout() },
                onOpenCalendar = { navController.navigate("${AppRoutes.CALENDAR_ROUTE}/$usuarioId") }
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
                onDiaSelected = { diaId -> navController.navigate("${AppRoutes.RUTINA_ROUTE}/$diaId") },
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
                onAgregarEjercicio = { navController.navigate("${AppRoutes.EJERCICIOS_ROUTE}/$diaId") },
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.EJERCICIOS,
            arguments = listOf(navArgument(AppRoutes.EJERCICIOS_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val diaId = backStackEntry.arguments?.getInt(AppRoutes.EJERCICIOS_ARG) ?: 0
            val rutinaRoute = "${AppRoutes.RUTINA_ROUTE}/$diaId"
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
                onDateSelected = { dateIso -> navController.navigate("${AppRoutes.DAILY_ROUTE}/$usuarioId/$dateIso") },
                onBack = { navController.popBackStack() }
            )
        }

        // Composable para el detalle diario (usuarioId y dateIso como argumentos)
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
    }
}