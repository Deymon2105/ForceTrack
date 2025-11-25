package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*        
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.forcetrack.database.entity.BloqueEntity
import com.example.forcetrack.viewmodel.BloquesViewModel
import com.example.forcetrack.ui.components.MotivationalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloquesScreen(
    usuarioId: Int,
    bloquesViewModel: BloquesViewModel,
    onBloqueSelected: (Int) -> Unit,
    onLogout: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var bloqueToDelete by remember { mutableStateOf<BloqueEntity?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val uiState by bloquesViewModel.uiState.collectAsState()

    // Snackbar para mostrar mensajes de error
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar errores en un Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            bloquesViewModel.clearError()
        }
    }

    if (showCreateDialog) {
        CreateBloqueDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, numSemanas, numDias ->
                bloquesViewModel.crearBloque(nombre, usuarioId, numSemanas, numDias)
                showCreateDialog = false
            }
        )
    }

    bloqueToDelete?.let {
        DeleteBloqueDialog(
            bloque = it,
            onDismiss = { bloqueToDelete = null },
            onConfirm = {
                bloquesViewModel.eliminarBloque(it.id)
                bloqueToDelete = null
            }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Bloques", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarToday, "Calendario")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Crear nuevo bloque")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading && uiState.bloques.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.bloques.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card motivador - Frase del día
                        item {
                            MotivationalCard(
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(items = uiState.bloques, key = { it.id }) { bloque ->
                            BloqueCard(
                                bloque = bloque,
                                onClick = { onBloqueSelected(bloque.id) },
                                onDelete = { bloqueToDelete = bloque }
                            )
                        }

                        // Mostrar indicador de carga al final si se está actualizando
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BloqueCard(bloque: BloqueEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(bloque.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar bloque", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(80.dp))
            Text("No hay bloques", style = MaterialTheme.typography.headlineSmall)
            Text("Toca el botón (+) para crear tu primer bloque.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateBloqueDialog(onDismiss: () -> Unit, onConfirm: (String, Int, Int) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var numSemanas by remember { mutableStateOf("4") }
    var numDias by remember { mutableStateOf("7") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Bloque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorMessage = null
                    },
                    label = { Text("Nombre del Bloque") },
                    isError = nombre.isBlank() && errorMessage != null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = numSemanas,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            numSemanas = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Número de Semanas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = numDias,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            numDias = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Días por semana (1-7)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> errorMessage = "El nombre no puede estar vacío"
                        numSemanas.toIntOrNull() == null || numSemanas.toInt() <= 0 ->
                            errorMessage = "Las semanas deben ser mayor a 0"
                        numDias.toIntOrNull() == null || numDias.toInt() !in 1..7 ->
                            errorMessage = "Los días deben estar entre 1 y 7"
                        else -> {
                            onConfirm(nombre, numSemanas.toInt(), numDias.toInt())
                        }
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun DeleteBloqueDialog(bloque: BloqueEntity, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Bloque") },
        text = { Text("¿Seguro que quieres eliminar el bloque '${bloque.nombre}'?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun LogoutConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Cerrar Sesión",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                "¿Estás seguro que deseas cerrar sesión?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cerrar Sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
