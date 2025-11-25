package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forcetrack.model.EjercicioRutina
import com.example.forcetrack.model.Serie
import com.example.forcetrack.viewmodel.RutinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinaDiariaScreen(
    rutinaViewModel: RutinaViewModel,
    onAgregarEjercicio: () -> Unit,
    onBackPressed: (() -> Unit)? = null
) {
    // CORRECCIÓN: Se observa el estado del ViewModel usando collectAsState()
    val dia by rutinaViewModel.diaActual.collectAsState()
    val ejercicios by rutinaViewModel.ejercicios.collectAsState()
    val isLoading by rutinaViewModel.cargando.collectAsState()

    // Estado para el diálogo de notas
    var mostrarDialogoNotas by remember { mutableStateOf(false) }
    // Estado para mostrar ayuda de RIR
    var mostrarAyudaRIR by remember { mutableStateOf(false) }

    // Diálogo de ayuda RIR
    if (mostrarAyudaRIR) {
        AlertDialog(
            onDismissRequest = { mostrarAyudaRIR = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("¿Qué es RIR?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "RIR = Reps In Reserve (Repeticiones en Reserva)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Indica cuántas repeticiones podrías hacer todavía:")
                    Text("• RIR 0 = Fallo muscular (0 reps más)")
                    Text("• RIR 1 = Podrías hacer 1 más")
                    Text("• RIR 2 = Podrías hacer 2 más")
                    Text("• RIR 3-5 = Muy lejos del fallo")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Recomendado: RIR 1-2 para hipertrofia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { mostrarAyudaRIR = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Diálogo de Notas mejorado
    if (mostrarDialogoNotas) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNotas = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Notas de la Sesión") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Registra cómo te sentiste, dolores, energía, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = dia?.notas ?: "",
                        onValueChange = { nuevasNotas -> rutinaViewModel.actualizarNotas(nuevasNotas) },
                        placeholder = { Text("Ej: Me sentí fuerte, sin dolores...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { mostrarDialogoNotas = false }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNotas = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            dia?.nombre ?: "...",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            "Registra tus series y repeticiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    if (onBackPressed != null) {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver al calendario")
                        }
                    }
                },
                actions = {
                    // Botón de ayuda RIR en el AppBar
                    IconButton(onClick = { mostrarAyudaRIR = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "¿Qué es RIR?",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón de Notas con etiqueta
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "Notas",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = { mostrarDialogoNotas = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Notes, "Añadir notas de la sesión", modifier = Modifier.size(20.dp))
                    }
                }

                // Botón principal de agregar ejercicio con etiqueta
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "Nuevo Ejercicio",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    FloatingActionButton(
                        onClick = onAgregarEjercicio,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, "Agregar un ejercicio a tu rutina")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ejercicios.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Icono principal con efecto visual
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Título principal
                        Text(
                            "¡Listo para Entrenar!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        // Subtítulo
                        Text(
                            "Agrega tu primer ejercicio para comenzar tu sesión de entrenamiento",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tarjeta de información con iconos
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Qué registrar:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Peso utilizado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "En kilogramos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "#",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Repeticiones",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Número de reps completadas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Timer,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "RIR (Reps en Reserva)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Qué tan cerca del fallo llegaste",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 200.dp  // Aumentado de 160dp a 200dp para evitar que los FABs tapen el contenido
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = ejercicios, key = { it.id }) { ejercicio ->
                        EjercicioCard(
                            ejercicio = ejercicio,
                            onAddSerie = { rutinaViewModel.agregarSerie(ejercicio.id) },
                            onDeleteSerie = { serieId -> rutinaViewModel.eliminarSerie(ejercicio.id, serieId) },
                            onUpdateSerie = { serie -> rutinaViewModel.actualizarSerie(serie) },
                            onDeleteEjercicio = { rutinaViewModel.eliminarEjercicio(ejercicio.id) },
                            onUpdateDescanso = { descanso -> rutinaViewModel.actualizarDescanso(ejercicio.id, descanso) },
                            isAddingSerieInProgress = rutinaViewModel.isOperacionEnProceso(ejercicio.id, "agregar_serie")
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun EjercicioCard(
    ejercicio: EjercicioRutina,
    onDeleteEjercicio: () -> Unit,
    onAddSerie: () -> Unit,
    onDeleteSerie: (Int) -> Unit,
    onUpdateSerie: (Serie) -> Unit,
    onUpdateDescanso: (Int) -> Unit,
    isAddingSerieInProgress: Boolean
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDescanso by remember { mutableStateOf(false) }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar Ejercicio") },
            text = { Text("¿Seguro que quieres eliminar '${ejercicio.nombre}' y todas sus series?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEjercicio()
                        mostrarDialogoEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header mejorado con diseño más moderno
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Icono del ejercicio
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            ejercicio.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (ejercicio.descanso > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${ejercicio.descanso}s descanso",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                "${ejercicio.series.size} series",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Botón de descanso con diseño mejorado
                    FilledTonalIconButton(
                        onClick = { mostrarDescanso = !mostrarDescanso },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (mostrarDescanso)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Configurar descanso entre series",
                            modifier = Modifier.size(20.dp),
                            tint = if (mostrarDescanso) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Botón eliminar ejercicio con diseño mejorado
                    FilledTonalIconButton(
                        onClick = { mostrarDialogoEliminar = true },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar ejercicio completo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Descanso expandible con ayuda
            if (mostrarDescanso) {
                var descansoText by remember(ejercicio.id, ejercicio.descanso) {
                    mutableStateOf(if (ejercicio.descanso == 0) "" else ejercicio.descanso.toString())
                }

                LaunchedEffect(ejercicio.descanso) {
                    val newDesc = if (ejercicio.descanso == 0) "" else ejercicio.descanso.toString()
                    if (descansoText != newDesc) descansoText = newDesc
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Tiempo de descanso entre series",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        OutlinedTextField(
                            value = descansoText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                descansoText = filtered
                                filtered.toIntOrNull()?.let { d ->
                                    val clamped = if (d < 0) 0 else d
                                    if (clamped != ejercicio.descanso) {
                                        if (d < 0) descansoText = clamped.toString()
                                        onUpdateDescanso(clamped)
                                    }
                                }
                            },
                            label = { Text("Segundos", style = MaterialTheme.typography.labelSmall) },
                            supportingText = { Text("Recomendado: 60-120s") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headers de columnas con diseño más moderno
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Set",
                        modifier = Modifier.width(40.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Peso (kg)",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Reps",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "RIR",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Series con mejor feedback visual
            ejercicio.series.forEachIndexed { index, serie ->
                SerieRow(
                    serieNum = index + 1,
                    serie = serie,
                    onUpdate = onUpdateSerie,
                    onDelete = { onDeleteSerie(serie.id) },
                    canDelete = ejercicio.series.size > 1
                )
            }

            // Botón grande para agregar serie al final con diseño mejorado
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onAddSerie,
                enabled = !isAddingSerieInProgress,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isAddingSerieInProgress) "Agregando..." else "Agregar Nueva Serie",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SerieRow(
    serieNum: Int,
    serie: Serie,
    onUpdate: (Serie) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var pesoText by remember(serie.id) { mutableStateOf(if (serie.peso == 0.0) "" else serie.peso.toString()) }
    var repsText by remember(serie.id) { mutableStateOf(if (serie.reps == 0) "" else serie.reps.toString()) }
    var rirText by remember(serie.id) { mutableStateOf(if (serie.rir == 0) "" else serie.rir.toString()) }

    LaunchedEffect(serie.id) {
        pesoText = if (serie.peso == 0.0) "" else serie.peso.toString()
        repsText = if (serie.reps == 0) "" else serie.reps.toString()
        rirText = if (serie.rir == 0) "" else serie.rir.toString()
    }

    // Determinar si la serie está completa
    val serieCompleta = serie.peso > 0 && serie.reps > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Indicador de número de serie con estado visual
        Surface(
            color = if (serieCompleta) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                   else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.width(32.dp)
        ) {
            Text(
                "#$serieNum",
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (serieCompleta) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Peso con mejor placeholder
        OutlinedTextField(
            value = pesoText,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' }
                val sanitized = if (filtered.count { it == '.' } > 1) {
                    filtered.filterIndexed { index, c ->
                        c != '.' || filtered.indexOf('.') == index
                    }
                } else filtered
                pesoText = sanitized
                val pesoValue = sanitized.toDoubleOrNull() ?: 0.0
                if (pesoValue != serie.peso) {
                    onUpdate(serie.copy(peso = pesoValue))
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            placeholder = {
                Text(
                    "0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )

        // Reps con mejor placeholder
        OutlinedTextField(
            value = repsText,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }
                repsText = filtered
                val repsValue = filtered.toIntOrNull() ?: 0
                if (repsValue != serie.reps) {
                    onUpdate(serie.copy(reps = repsValue))
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = {
                Text(
                    "0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )

        // RIR con código de colores y feedback visual mejorado
        OutlinedTextField(
            value = rirText,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }
                val sanitized = if (filtered.isNotEmpty()) {
                    val value = filtered.toIntOrNull() ?: 0
                    val clamped = value.coerceIn(0, 5)
                    clamped.toString()
                } else {
                    filtered
                }
                rirText = sanitized
                val rirValue = sanitized.toIntOrNull() ?: 0
                if (rirValue != serie.rir) {
                    onUpdate(serie.copy(rir = rirValue))
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = {
                Text(
                    "1-2",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when (serie.rir) {
                    0, 1, 2 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                unfocusedBorderColor = when (serie.rir) {
                    0, 1, 2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                }
            )
        )

        // Botón de eliminar serie mejorado
        if (canDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar esta serie",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}
