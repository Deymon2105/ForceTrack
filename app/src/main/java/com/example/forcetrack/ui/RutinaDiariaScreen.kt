package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forcetrack.viewmodel.RutinaViewModel
import com.example.forcetrack.model.EjercicioRutina

@Composable
fun RutinaDiariaScreen(rutinaViewModel: RutinaViewModel, onAgregarEjercicio: () -> Unit) {
    val rutina = rutinaViewModel.diaRutina.value
    var nota by remember { mutableStateOf(rutina?.notas ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Rutina de ${rutina?.nombre ?: ""}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            items(rutina?.ejercicios?.size ?: 0) { idx ->
                val ejercicio = rutina?.ejercicios?.get(idx) ?: return@items
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(ejercicio.nombre, style = MaterialTheme.typography.titleMedium)
                        Row {
                            OutlinedTextField(
                                value = ejercicio.peso.toString(),
                                onValueChange = { ejercicio.peso = it.toDoubleOrNull() ?: 0.0 },
                                label = { Text("Peso") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ejercicio.reps.toString(),
                                onValueChange = { ejercicio.reps = it.toIntOrNull() ?: 0 },
                                label = { Text("Reps") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ejercicio.rir.toString(),
                                onValueChange = { ejercicio.rir = it.toIntOrNull() ?: 0 },
                                label = { Text("RIR") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ejercicio.descanso.toString(),
                                onValueChange = { ejercicio.descanso = it.toIntOrNull() ?: 0 },
                                label = { Text("Descanso") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        TextButton(onClick = { rutinaViewModel.eliminarEjercicio(idx) }) {
                            Text("Eliminar Ejercicio")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        Button(onClick = onAgregarEjercicio) {
            Text("Agregar Ejercicio")
        }
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = nota,
            onValueChange = {
                nota = it
                rutinaViewModel.actualizarNota(nota)
            },
            label = { Text("Notas") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
