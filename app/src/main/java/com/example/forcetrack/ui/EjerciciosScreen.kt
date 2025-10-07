package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forcetrack.model.EjercicioDisponible
import com.example.forcetrack.repository.MockRepository

@Composable
fun EjerciciosScreen(onEjercicioAdd: (EjercicioDisponible) -> Unit) {
    val ejercicios = MockRepository.ejerciciosDisponibles

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Selecciona Ejercicio", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn {
            ejercicios.groupBy { it.tipo }.forEach { (tipo, lista) ->
                item { Text(tipo, style = MaterialTheme.typography.titleMedium) }
                items(lista.size) { idx ->
                    val ejercicio = lista[idx]
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { onEjercicioAdd(ejercicio) }
                    ) {
                        Text(ejercicio.nombre)
                    }
                }
            }
        }
    }
}
