package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forcetrack.model.BloqueEntrenamiento
import com.example.forcetrack.model.DiaRutina

@Composable
fun SplitSemanalScreen(bloque: BloqueEntrenamiento, onDiaSelected: (DiaRutina) -> Unit) {
    val semanaActual = bloque.semanas.first()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Semana ${semanaActual.numero}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn {
            items(semanaActual.dias.size) { idx ->
                val dia = semanaActual.dias[idx]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), onClick = { onDiaSelected(dia) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(dia.nombre, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
