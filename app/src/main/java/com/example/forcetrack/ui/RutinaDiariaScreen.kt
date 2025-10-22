package com.example.forcetrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutina de ${dia?.nombre ?: "..."}") },
                navigationIcon = {
                    if (onBackPressed != null) {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (ejercicios.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay ejercicios. Toca \"Agregar Ejercicio\" para empezar.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = ejercicios, key = { it.id }) { ejercicio ->
                            EjercicioCard(
                                ejercicio = ejercicio,
                                onAddSerie = { rutinaViewModel.agregarSerie(ejercicio.id) },
                                onDeleteSerie = { serieId -> rutinaViewModel.eliminarSerie(ejercicio.id, serieId) },
                                onUpdateSerie = { serie -> rutinaViewModel.actualizarSerie(serie) },
                                onDeleteEjercicio = { rutinaViewModel.eliminarEjercicio(ejercicio.id) },
                                onUpdateDescanso = { descanso -> rutinaViewModel.actualizarDescanso(ejercicio.id, descanso) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAgregarEjercicio,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Agregar Ejercicio", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dia?.notas ?: "",
                    onValueChange = { nuevasNotas -> rutinaViewModel.actualizarNotas(nuevasNotas) },
                    label = { Text("Notas de la sesión") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
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
    onUpdateDescanso: (Int) -> Unit
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar Ejercicio") },
            text = { Text("¿Seguro que quieres eliminar '${ejercicio.nombre}' de tu rutina?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEjercicio()
                        mostrarDialogoEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(ejercicio.nombre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onAddSerie) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Serie")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.padding(horizontal = 16.dp)) {
                    Text("Set", modifier = Modifier.width(40.dp), style = MaterialTheme.typography.bodySmall)
                    Text("kg", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text("Reps", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text("RIR", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(40.dp))
                }

                // MEJORA: Se itera directamente. La clave ya está en el `LazyColumn` padre.
                ejercicio.series.forEachIndexed { index, serie ->
                    SerieRow(
                        serieNum = index + 1,
                        serie = serie,
                        onUpdate = onUpdateSerie,
                        onDelete = { onDeleteSerie(serie.id) },
                        canDelete = ejercicio.series.size > 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var descansoText by remember(ejercicio.descanso) { mutableStateOf(ejercicio.descanso.toString()) }
            OutlinedTextField(
                value = descansoText,
                onValueChange = {
                    descansoText = it
                    it.toIntOrNull()?.let { descanso -> onUpdateDescanso(descanso) }
                },
                label = { Text("Descanso (segundos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { mostrarDialogoEliminar = true }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Eliminar Ejercicio", color = MaterialTheme.colorScheme.error)
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("#$serieNum", modifier = Modifier.width(40.dp), style = MaterialTheme.typography.bodyLarge)

        EditableTextField(
            value = if (serie.peso == 0.0) "" else serie.peso.toString(),
            onValueChange = { newValue ->
                val peso = newValue.toDoubleOrNull() ?: 0.0
                onUpdate(serie.copy(peso = peso))
            }
        )

        EditableTextField(
            value = if (serie.reps == 0) "" else serie.reps.toString(),
            onValueChange = {
                val reps = it.toIntOrNull() ?: 0
                onUpdate(serie.copy(reps = reps))
            }
        )

        EditableTextField(
            value = if (serie.rir == 0) "" else serie.rir.toString(),
            onValueChange = {
                val rir = it.toIntOrNull() ?: 0
                onUpdate(serie.copy(rir = rir))
            }
        )

        if (canDelete) {
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, "Eliminar serie")
            }
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun RowScope.EditableTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.weight(1f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}
