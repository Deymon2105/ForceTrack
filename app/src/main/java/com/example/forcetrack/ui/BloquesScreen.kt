package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forcetrack.model.BloqueEntrenamiento
import com.example.forcetrack.model.SemanaEntrenamiento
import com.example.forcetrack.model.DiaRutina
import com.example.forcetrack.viewmodel.BloquesViewModel

@Composable
fun BloquesScreen(bloquesViewModel: BloquesViewModel, onBloqueSelected: (BloqueEntrenamiento) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bloques de Entrenamiento", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            items(bloquesViewModel.bloques.size) { i ->
                val bloque = bloquesViewModel.bloques[i]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), onClick = { onBloqueSelected(bloque) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(bloque.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("${bloque.semanas.size} semanas")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            val newBloque = BloqueEntrenamiento(
                id = bloquesViewModel.bloques.size + 1,
                nombre = "Nuevo Bloque",
                semanas = listOf(
                    SemanaEntrenamiento(1, listOf(
                        DiaRutina("Día 1"), DiaRutina("Día 2")
                    ))
                )
            )
            bloquesViewModel.agregarBloque(newBloque)
        }) {
            Text("Agregar Bloque")
        }
    }
}
