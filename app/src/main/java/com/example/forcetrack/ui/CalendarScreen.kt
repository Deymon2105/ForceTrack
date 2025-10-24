package com.example.forcetrack.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.forcetrack.database.entity.TrainingLogEntity
import com.example.forcetrack.viewmodel.TrainingLogViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    usuarioId: Int,
    trainingLogViewModel: TrainingLogViewModel,
    onDateSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by trainingLogViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Estado del mes mostrado
    val displayedMonth = remember { mutableStateOf(Calendar.getInstance()) }
    var previewNotes by remember { mutableStateOf<String?>(null) }
    var previewDate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuarioId) {
        trainingLogViewModel.loadLogs(usuarioId)
    }

    fun formatIsoFrom(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return fmt.format(cal.time)
    }

    // Calcula la racha actual; si ignoreWeekends=true, los fines de semana no rompen la racha
    fun calculateStreak(loggedDates: Set<String>, ignoreWeekends: Boolean = false): Int {
        var streak = 0
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        repeat(365) {
            val iso = fmt.format(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
            if (loggedDates.contains(iso)) {
                streak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                if (ignoreWeekends && isWeekend) {
                    // No contamos el fin de semana como ruptura; simplemente retrocedemos sin incrementar
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    return@repeat
                }
                // fin de la racha
                return streak
            }
        }
        return streak
    }

    // Devuelve el prefijo YYYY-MM para el mes mostrado
    fun monthPrefix(year: Int, month: Int): String {
        val mm = month + 1
        val mmStr = if (mm < 10) "0$mm" else "$mm"
        return "${year}-$mmStr"
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                val monthLabel = SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(displayedMonth.value.time)
                Text("Calendario — $monthLabel")
            }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                coroutineScope.launch {
                    val existing = trainingLogViewModel.getLogByDate(usuarioId, todayIso)
                    if (existing == null) {
                        trainingLogViewModel.saveLog(TrainingLogEntity(usuarioId = usuarioId, dateIso = todayIso, notas = ""))
                    }
                    onDateSelected(todayIso)
                }
            }) {
                Icon(Icons.Default.CalendarToday, "Hoy")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.errorMessage}")
                }
                else -> {
                    // Preparar datos auxiliares
                    val logsByDate = uiState.logs.associateBy { it.dateIso }
                    val loggedDatesSet = logsByDate.keys.toSet()

                    // Resumen mensual y racha
                    val calForSummary = displayedMonth.value
                    val year = calForSummary.get(Calendar.YEAR)
                    val month = calForSummary.get(Calendar.MONTH)
                    val prefix = monthPrefix(year, month)
                    val daysThisMonthCount = logsByDate.keys.count { it.startsWith(prefix) }
                    val currentStreak = calculateStreak(loggedDatesSet, ignoreWeekends = false)
                    val weekdayStreak = calculateStreak(loggedDatesSet, ignoreWeekends = true)

                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Cabecera resumen con animación slide al cambiar el mes
                        val monthIndex = year * 12 + month
                        AnimatedContent(targetState = monthIndex, transitionSpec = {
                            if (targetState > initialState) {
                                // Entrando desde la derecha, saliendo a la izquierda
                                (slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) +
                                        fadeIn(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) +
                                                fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)))
                            } else {
                                // Entrando desde la izquierda, saliendo a la derecha
                                (slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) +
                                        fadeIn(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) +
                                                fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)))
                            }
                        }) { monthIndexValue ->
                            // Consumimos el parámetro para evitar advertencias
                            key(monthIndexValue) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Días entrenados este mes: $daysThisMonthCount", style = MaterialTheme.typography.bodyMedium)
                                        Text("Racha actual: $currentStreak días · Racha (sin fines de semana): $weekdayStreak", style = MaterialTheme.typography.bodySmall)
                                    }
                                    // Botones de navegación del mes
                                    Row {
                                        IconButton(onClick = { displayedMonth.value.add(Calendar.MONTH, -1) }) { Text("<") }
                                        IconButton(onClick = { displayedMonth.value = Calendar.getInstance() }) { Text("Hoy") }
                                        IconButton(onClick = { displayedMonth.value.add(Calendar.MONTH, 1) }) { Text(">") }
                                    }
                                }
                            }
                        }

                        // Días de la semana
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val weekDays = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                            for (d in weekDays) {
                                Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }

                        // Calcular días del mes y mostrar la grilla (animated content)
                        val cal = displayedMonth.value
                        val yearGrid = cal.get(Calendar.YEAR)
                        val monthGrid = cal.get(Calendar.MONTH)
                        // clonamos para no modificar el estado
                        val firstOfMonth = Calendar.getInstance().also {
                            it.set(Calendar.YEAR, yearGrid)
                            it.set(Calendar.MONTH, monthGrid)
                            it.set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val firstDayOfWeek = firstOfMonth.get(Calendar.DAY_OF_WEEK) // 1=Dom, 2=Lun...
                        val offset = firstDayOfWeek - Calendar.SUNDAY // 0..6
                        val totalCells = (daysInMonth + offset)
                        val rows = ceil(totalCells / 7.0).toInt()

                        // Animación slide para la grilla cuando cambie el mes (usamos key del timeInMillis)
                        AnimatedContent(targetState = displayedMonth.value.timeInMillis, transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) +
                                        fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) +
                                                fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)))
                            } else {
                                (slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) +
                                        fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) +
                                                fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)))
                            }
                        }) { targetMillis ->
                            // Consumimos el parámetro para evitar advertencias
                            key(targetMillis) {
                                Column {
                                    for (row in 0 until rows) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            for (col in 0..6) {
                                                val cellIndex = row * 7 + col
                                                val dayNumber = cellIndex - offset + 1
                                                if (dayNumber in 1..daysInMonth) {
                                                    val iso = formatIsoFrom(yearGrid, monthGrid, dayNumber)
                                                    val logForIso = logsByDate[iso]
                                                    val hasLog = logForIso != null
                                                    val hasNotes = !logForIso?.notas.isNullOrBlank()
                                                    Surface(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(88.dp)
                                                            .padding(2.dp)
                                                            .combinedClickable(
                                                                onClick = {
                                                                    coroutineScope.launch {
                                                                        val existing = trainingLogViewModel.getLogByDate(usuarioId, iso)
                                                                        if (existing == null) {
                                                                            trainingLogViewModel.saveLog(TrainingLogEntity(usuarioId = usuarioId, dateIso = iso, notas = ""))
                                                                        }
                                                                        onDateSelected(iso)
                                                                    }
                                                                },
                                                                onLongClick = {
                                                                    previewNotes = logForIso?.notas
                                                                    previewDate = iso
                                                                }
                                                            ),
                                                        color = if (hasLog) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                                        tonalElevation = if (hasLog) 2.dp else 0.dp
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.Center) {
                                                                // Número del día
                                                                Text(dayNumber.toString(), textAlign = TextAlign.Center)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                if (hasNotes) {
                                                                     val notasText = logForIso.notas
                                                                    val firstLine = notasText.lineSequence().firstOrNull()?.trim().orEmpty()
                                                                    Text(
                                                                        text = firstLine,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        maxLines = 2,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                        modifier = Modifier.fillMaxWidth()
                                                                    )
                                                                } else if (hasLog) {
                                                                    Box(modifier = Modifier
                                                                        .size(8.dp)
                                                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Box(modifier = Modifier.weight(1f).height(64.dp)) { /* empty */ }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dialog de previsualización de notas al long-press
                        if (previewNotes != null) {
                            AlertDialog(
                                onDismissRequest = { previewNotes = null; previewDate = null },
                                title = { Text(previewDate ?: "Notas") },
                                text = { Text(previewNotes ?: "") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        // Abrir el registro completo
                                        val pd = previewDate
                                        previewNotes = null
                                        previewDate = null
                                        if (pd != null) onDateSelected(pd)
                                    }) { Text("Abrir") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { previewNotes = null; previewDate = null }) { Text("Cerrar") }
                                }
                            )
                        }

                        // Lista rápida de logs como fallback/visual
                        if (uiState.logs.isNotEmpty()) {
                            Text("Registros recientes", style = MaterialTheme.typography.titleMedium)
                            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                                items(items = uiState.logs.sortedByDescending { it.dateIso }, key = { it.id }) { log ->
                                    Card(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onDateSelected(log.dateIso) }) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(log.dateIso)
                                            Text(if (log.notas.isNullOrBlank()) "Sin notas" else "Con notas")
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
