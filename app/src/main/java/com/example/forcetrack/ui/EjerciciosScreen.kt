package com.example.forcetrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forcetrack.model.EjercicioDisponible

// Lista estática de ejercicios disponibles. En una aplicación real, esto provendría de la base de datos.
private val ejerciciosDisponibles = listOf(
    EjercicioDisponible("Pecho", "Press de Banca"),
    EjercicioDisponible("Pecho", "Press Inclinado con Mancuernas"),
    EjercicioDisponible("Pecho", "Aperturas con Mancuernas"),
    EjercicioDisponible("Espalda", "Dominadas"),
    EjercicioDisponible("Espalda", "Remo con Barra"),
    EjercicioDisponible("Espalda", "Jalón al Pecho"),
    EjercicioDisponible("Pierna", "Sentadillas"),
    EjercicioDisponible("Pierna", "Prensa de Piernas"),
    EjercicioDisponible("Pierna", "Zancadas"),
    EjercicioDisponible("Hombro", "Press Militar"),
    EjercicioDisponible("Hombro", "Elevaciones Laterales"),
    EjercicioDisponible("Bícep", "Curl con Barra"),
    EjercicioDisponible("Trícep", "Press Francés")
).sortedBy { it.nombre }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjerciciosScreen(
    onEjercicioAdd: (EjercicioDisponible) -> Unit,
    onBackPressed: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filtra la lista de ejercicios basándose en la búsqueda del usuario.
    val filteredEjercicios = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ejerciciosDisponibles
        } else {
            ejerciciosDisponibles.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) || 
                it.tipo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Ejercicio") },
                navigationIcon = { 
                    IconButton(onClick = onBackPressed) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") 
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text("Buscar por nombre o grupo muscular...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredEjercicios.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron ejercicios.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Agrupa los ejercicios por tipo (Pecho, Espalda, etc.)
                    filteredEjercicios.groupBy { it.tipo }.forEach { (tipo, ejercicios) ->
                        item {
                            Text(
                                text = tipo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(ejercicios) { ejercicio ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onEjercicioAdd(ejercicio) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ejercicio.nombre, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Add, contentDescription = "Añadir ejercicio")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}