package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.forcetrack.database.entity.TrainingLogEntity
import com.example.forcetrack.viewmodel.TrainingLogViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    usuarioId: Int,
    dateIso: String,
    trainingLogViewModel: TrainingLogViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var currentLog by remember { mutableStateOf<TrainingLogEntity?>(null) }
    var notas by remember { mutableStateOf(TextFieldValue("")) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(dateIso) {
        loading = true
        val log = trainingLogViewModel.getLogByDate(usuarioId, dateIso)
        currentLog = log
        notas = TextFieldValue(log?.notas ?: "")
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Registro: $dateIso") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas del entrenamiento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 6
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            coroutineScope.launch {
                                val logToSave = TrainingLogEntity(
                                    id = currentLog?.id ?: 0,
                                    usuarioId = usuarioId,
                                    dateIso = dateIso,
                                    notas = notas.text
                                )
                                trainingLogViewModel.saveLog(logToSave)
                                onBack()
                            }
                        }) {
                            Text("Guardar")
                        }

                        if (currentLog != null) {
                            Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), onClick = {
                                coroutineScope.launch {
                                    trainingLogViewModel.deleteLogById(currentLog!!.id)
                                    onBack()
                                }
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
