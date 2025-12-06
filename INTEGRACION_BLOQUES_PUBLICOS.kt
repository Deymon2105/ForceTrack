// ================================================
// INTEGRACIÓN DE BLOQUES PÚBLICOS EN NAVEGACIÓN
// Archivo: MainActivity.kt
// ================================================

// 1. Importar la pantalla
import com.example.forcetrack.ui.BloquesPublicosScreen

// 2. En AppNavigation, agregar la ruta en NavHost:

@Composable
fun AppNavigation(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // ... rutas existentes ...
        
        composable(AppRoutes.BLOQUES) {
            BloquesScreen(
                navController = navController,
                viewModelFactory = viewModelFactory
            )
        }
        
        // ✨ NUEVA RUTA: Bloques Públicos
        composable("bloques_publicos") {
            BloquesPublicosScreen(
                navController = navController
            )
        }
        
        // ✨ NUEVA RUTA: Detalle de Bloque Público (futuro)
        composable(
            route = "bloque_publico_detalle/{bloqueId}",
            arguments = listOf(navArgument("bloqueId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bloqueId = backStackEntry.arguments?.getInt("bloqueId") ?: 0
            BloquePublicoDetalleScreen(
                navController = navController,
                bloqueId = bloqueId,
                viewModelFactory = viewModelFactory
            )
        }
        
        // ... más rutas ...
    }
}

// ================================================
// AGREGAR BOTÓN EN LA PANTALLA PRINCIPAL
// Archivo: BloquesScreen.kt o cualquier pantalla principal
// ================================================

@Composable
fun BloquesScreen(
    navController: NavController,
    viewModelFactory: ViewModelFactory
) {
    Scaffold(
        topBar = { /* ... */ },
        floatingActionButton = {
            // Botón existente para crear bloque
            FloatingActionButton(onClick = { /* crear bloque */ }) {
                Icon(Icons.Default.Add, "Crear")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            
            // ✨ NUEVO: Botón para ver bloques públicos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { navController.navigate("bloques_publicos") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                "Explorar Rutinas Públicas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Descubre rutinas de la comunidad",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Lista de bloques del usuario
            Text(
                "Mis Bloques",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // ... resto del contenido ...
        }
    }
}

// ================================================
// AGREGAR SWITCH PARA VISIBILIDAD EN CADA BLOQUE
// Modificar el Card de cada bloque en BloquesScreen
// ================================================

@Composable
fun BloqueCard(
    bloque: BloqueEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: (Boolean) -> Unit  // ✨ NUEVO
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título y categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = bloque.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bloque.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // ✨ NUEVO: Indicador de bloque público
                if (bloque.esPublico) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Público",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ✨ NUEVO: Switch para cambiar visibilidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (bloque.esPublico) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (bloque.esPublico) "Rutina pública" else "Rutina privada",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Switch(
                    checked = bloque.esPublico,
                    onCheckedChange = onToggleVisibility
                )
            }
            
            // Botones de acción existentes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

// ================================================
// VIEWMODEL: Agregar función para cambiar visibilidad
// Archivo: BloquesViewModel.kt
// ================================================

class BloquesViewModel(
    private val repository: ForceTrackRepository,
    private val sessionManager: SessionManager,
    private val syncService: SyncService
) : ViewModel() {
    
    // ... código existente ...
    
    // ✨ NUEVO: Cambiar visibilidad de un bloque
    fun cambiarVisibilidad(bloqueId: Int, esPublico: Boolean) {
        viewModelScope.launch {
            try {
                // Actualizar en el backend
                val response = RetrofitClient.api.cambiarVisibilidadBloque(
                    bloqueId = bloqueId,
                    esPublico = esPublico
                )
                
                if (response.isSuccessful) {
                    // Actualizar en la base de datos local
                    // (necesitarás agregar este método al DAO)
                    repository.actualizarVisibilidadBloque(bloqueId, esPublico)
                    
                    _mensaje.value = if (esPublico) {
                        "Bloque ahora es público y visible para todos"
                    } else {
                        "Bloque ahora es privado"
                    }
                } else {
                    _error.value = "Error al cambiar visibilidad"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}

// ================================================
// DAO: Agregar método para actualizar visibilidad
// Archivo: BloqueDao.kt
// ================================================

@Dao
interface BloqueDao {
    // ... métodos existentes ...
    
    // ✨ NUEVO: Actualizar solo la visibilidad
    @Query("UPDATE bloques SET esPublico = :esPublico WHERE id = :bloqueId")
    suspend fun actualizarVisibilidad(bloqueId: Int, esPublico: Boolean)
}

// ================================================
// REPOSITORY: Agregar método wrapper
// Archivo: ForceTrackRepository.kt
// ================================================

class ForceTrackRepository(/* ... */) {
    // ... código existente ...
    
    // ✨ NUEVO: Cambiar visibilidad
    suspend fun actualizarVisibilidadBloque(bloqueId: Int, esPublico: Boolean) {
        bloqueDao.actualizarVisibilidad(bloqueId, esPublico)
    }
}

// ================================================
// USO EN LA UI
// Archivo: BloquesScreen.kt
// ================================================

@Composable
fun BloquesScreen(
    navController: NavController,
    viewModelFactory: ViewModelFactory
) {
    val viewModel: BloquesViewModel = viewModel(factory = viewModelFactory)
    val bloques by viewModel.bloques.collectAsState()
    
    LazyColumn {
        items(bloques) { bloque ->
            BloqueCard(
                bloque = bloque,
                onEdit = { /* editar */ },
                onDelete = { /* eliminar */ },
                onToggleVisibility = { esPublico ->
                    // ✨ Cambiar visibilidad
                    viewModel.cambiarVisibilidad(bloque.id, esPublico)
                }
            )
        }
    }
}
