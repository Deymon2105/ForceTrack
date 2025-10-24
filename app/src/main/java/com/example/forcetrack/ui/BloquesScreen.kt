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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloquesScreen(
    bloquesViewModel: BloquesViewModel,
    onBloqueSelected: (Int) -> Unit,
    onLogout: () -> Unit,
    onOpenCalendar: () -> Unit // añadido
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var bloqueToDelete by remember { mutableStateOf<BloqueEntity?>(null) }
    val uiState by bloquesViewModel.uiState.collectAsState()

    if (showCreateDialog) {
        CreateBloqueDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, numSemanas, numDias ->
                // TODO: El usuarioId debería venir del AuthViewModel
                bloquesViewModel.crearBloque(nombre, 1 , numSemanas, numDias)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Bloques", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarToday, "Calendario")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Crear nuevo bloque")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.errorMessage}")
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
                        items(items = uiState.bloques, key = { it.id }) { bloque ->
                            BloqueCard(
                                bloque = bloque,
                                onClick = { onBloqueSelected(bloque.id) },
                                onDelete = { bloqueToDelete = bloque }
                            )
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

@Composable
private fun CreateBloqueDialog(onDismiss: () -> Unit, onConfirm: (String, Int, Int) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var numSemanas by remember { mutableStateOf("4") }
    var numDias by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Bloque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Bloque") })
                OutlinedTextField(value = numSemanas, onValueChange = { numSemanas = it }, label = { Text("Número de Semanas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = numDias, onValueChange = { numDias = it }, label = { Text("Días por semana (1-7)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, numSemanas.toIntOrNull() ?: 4, numDias.toIntOrNull() ?: 7) }) {
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
