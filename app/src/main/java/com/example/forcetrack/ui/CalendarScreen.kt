package com.example.forcetrack.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.forcetrack.database.entity.TrainingLogEntity
import com.example.forcetrack.viewmodel.TrainingLogViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    usuarioId: Int,
    trainingLogViewModel: TrainingLogViewModel,
    onDateSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by trainingLogViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Estado para el mes que se está visualizando
    // Usamos un Calendar mutable pero recordado
    val displayedMonth = remember { mutableStateOf(Calendar.getInstance()) }
    
    // Formateadores de fecha (reutilizados)
    val isoFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthYearFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthNameFormatter = remember { SimpleDateFormat("MMMM", Locale.getDefault()) }

    // Fecha actual para referencias visuales
    val today = remember { Calendar.getInstance() }
    val todayIso = remember { isoFormatter.format(today.time) }

    // Cargar logs al iniciar
    LaunchedEffect(usuarioId) {
        trainingLogViewModel.loadLogs(usuarioId)
    }

    // Funciones auxiliares
    fun changeMonth(amount: Int) {
        val newCal = displayedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, amount)
        displayedMonth.value = newCal
    }

    fun getDaysInMonth(cal: Calendar): Int {
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeek(cal: Calendar): Int {
        val temp = cal.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)
        return temp.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...
    }

    // Lógica de Racha (Streak)
    val logsByDate = remember(uiState.logs) { uiState.logs.associateBy { it.dateIso } }
    val currentStreak = remember(uiState.logs) {
        var streak = 0
        val cal = Calendar.getInstance()
        // Retroceder desde hoy
        while (true) {
            val iso = isoFormatter.format(cal.time)
            if (logsByDate.containsKey(iso)) {
                streak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                // Si es hoy y no hay log, no rompemos la racha todavía (el usuario podría entrenar más tarde)
                if (iso == todayIso) {
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    continue
                }
                break
            }
        }
        streak
    }
    
    val sessionsThisMonth = remember(uiState.logs, displayedMonth.value.timeInMillis) {
        val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(displayedMonth.value.time)
        uiState.logs.count { it.dateIso.startsWith(currentMonthStr) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Calendario",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Ir a hoy
                    displayedMonth.value = Calendar.getInstance()
                    // Crear log si no existe y navegar
                    coroutineScope.launch {
                        if (!logsByDate.containsKey(todayIso)) {
                            trainingLogViewModel.saveLog(TrainingLogEntity(usuarioId = usuarioId, dateIso = todayIso, notas = ""))
                        }
                        onDateSelected(todayIso)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.CalendarToday, null) },
                text = { Text("Hoy") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Tarjeta de Resumen (Stats) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Racha
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF5722), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$currentStreak días", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Racha Actual", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    // Separador vertical
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )

                    // Sesiones Mes
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$sessionsThisMonth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Este Mes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // --- Navegación de Mes ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { changeMonth(-1) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Mes Anterior")
                }

                Text(
                    text = monthYearFormatter.format(displayedMonth.value.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = { changeMonth(1) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Mes Siguiente")
                }
            }

            // --- Calendario Grid ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Días de la semana
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    val weekDays = listOf("D", "L", "M", "M", "J", "V", "S")
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Días del mes
                val daysInMonth = getDaysInMonth(displayedMonth.value)
                val firstDayOfWeek = getFirstDayOfWeek(displayedMonth.value) // 1=Dom, 2=Lun...
                val offset = firstDayOfWeek - 1 // 0 para Domingo, 1 para Lunes...
                
                val totalSlots = daysInMonth + offset
                val rows = ceil(totalSlots / 7.0).toInt()

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..6) {
                            val index = row * 7 + col
                            val dayNumber = index - offset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cal = displayedMonth.value.clone() as Calendar
                                cal.set(Calendar.DAY_OF_MONTH, dayNumber)
                                val dateIso = isoFormatter.format(cal.time)
                                val isToday = dateIso == todayIso
                                val hasLog = logsByDate.containsKey(dateIso)
                                val log = logsByDate[dateIso]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                hasLog -> MaterialTheme.colorScheme.primaryContainer
                                                isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday && !hasLog) 2.dp else 0.dp,
                                            color = if (isToday && !hasLog) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .combinedClickable(
                                            onClick = {
                                                coroutineScope.launch {
                                                    if (!hasLog) {
                                                        trainingLogViewModel.saveLog(TrainingLogEntity(usuarioId = usuarioId, dateIso = dateIso, notas = ""))
                                                    }
                                                    onDateSelected(dateIso)
                                                }
                                            },
                                            onLongClick = {
                                                // Opcional: Mostrar tooltip o preview
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (hasLog || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (hasLog) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (hasLog) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Espacio vacío
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Espacio extra al final para el FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
