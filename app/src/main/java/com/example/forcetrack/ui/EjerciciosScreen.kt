package com.example.forcetrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forcetrack.model.EjercicioDisponible

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjerciciosScreen(
    ejerciciosDisponibles: List<EjercicioDisponible>,
    onEjercicioAdd: (EjercicioDisponible) -> Unit,
    onBackPressed: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filtra la lista de ejercicios basándose en la búsqueda del usuario.
    val filteredEjercicios = remember(searchQuery, ejerciciosDisponibles) {
        if (searchQuery.isBlank()) {
            ejerciciosDisponibles
        } else {
            ejerciciosDisponibles.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) || 
                it.tipo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Biblioteca de Ejercicios", fontWeight = FontWeight.Bold) },
                navigationIcon = { 
                    IconButton(onClick = onBackPressed) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") 
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            // Barra de búsqueda mejorada
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar (ej. Press Banca, Yoga...)") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredEjercicios.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No encontramos ese ejercicio", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Agrupa los ejercicios por tipo
                    filteredEjercicios.groupBy { it.tipo }.forEach { (tipo, ejercicios) ->
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            ) {
                                Icon(
                                    getIconForType(tipo),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tipo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        items(ejercicios) { ejercicio ->
                            EjercicioItemCard(
                                ejercicio = ejercicio,
                                onClick = { onEjercicioAdd(ejercicio) }
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
fun EjercicioItemCard(ejercicio: EjercicioDisponible, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                ejercicio.nombre, 
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Añadir",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun getIconForType(tipo: String): ImageVector {
    return when {
        tipo.contains("Gym", true) -> Icons.Default.FitnessCenter
        tipo.contains("Calistenia", true) -> Icons.Default.SelfImprovement // Aproximación
        tipo.contains("Boxeo", true) || tipo.contains("MMA", true) -> Icons.Default.SportsMartialArts
        tipo.contains("Yoga", true) -> Icons.Default.SelfImprovement
        tipo.contains("Cardio", true) -> Icons.Default.Timer
        else -> Icons.Default.FitnessCenter
    }
}