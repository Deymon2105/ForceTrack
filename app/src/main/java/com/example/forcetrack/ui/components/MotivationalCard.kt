package com.example.forcetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class FitnessColorScheme(
    val startColor: Color,
    val middleColor: Color,
    val endColor: Color,
    val icon: ImageVector
)

data class MotivationalQuote(
    val quote: String,
    val author: String,
    val colorScheme: FitnessColorScheme
)

object FitnessColors {
    // Esquemas de colores inspirados en fitness
    val EnergyRed = FitnessColorScheme(
        startColor = Color(0xFFFF416C),
        middleColor = Color(0xFFFF4B2B),
        endColor = Color(0xFFFF6B6B),
        icon = Icons.Default.FitnessCenter
    )

    val PowerPurple = FitnessColorScheme(
        startColor = Color(0xFF667EEA),
        middleColor = Color(0xFF764BA2),
        endColor = Color(0xFF8B5CF6),
        icon = Icons.Default.FitnessCenter
    )

    val StrengthOrange = FitnessColorScheme(
        startColor = Color(0xFFFF9A56),
        middleColor = Color(0xFFFF6B6B),
        endColor = Color(0xFFFFD93D),
        icon = Icons.Default.SportsGymnastics
    )

    val VitalityGreen = FitnessColorScheme(
        startColor = Color(0xFF11998E),
        middleColor = Color(0xFF38EF7D),
        endColor = Color(0xFF56CCF2),
        icon = Icons.AutoMirrored.Filled.DirectionsRun
    )

    val FocusBlue = FitnessColorScheme(
        startColor = Color(0xFF2E3192),
        middleColor = Color(0xFF1BFFFF),
        endColor = Color(0xFF4FACFE),
        icon = Icons.Default.SelfImprovement
    )

    val MotivationPink = FitnessColorScheme(
        startColor = Color(0xFFFA709A),
        middleColor = Color(0xFFFEE140),
        endColor = Color(0xFFFFB88C),
        icon = Icons.Default.FitnessCenter
    )

    val DeterminationTeal = FitnessColorScheme(
        startColor = Color(0xFF00C9FF),
        middleColor = Color(0xFF92FE9D),
        endColor = Color(0xFF00F2FE),
        icon = Icons.AutoMirrored.Filled.DirectionsRun
    )

    val PassionMagenta = FitnessColorScheme(
        startColor = Color(0xFFEB3349),
        middleColor = Color(0xFFF45C43),
        endColor = Color(0xFFFC466B),
        icon = Icons.Default.FitnessCenter
    )

    val EnduranceIndigo = FitnessColorScheme(
        startColor = Color(0xFF4A00E0),
        middleColor = Color(0xFF8E2DE2),
        endColor = Color(0xFFDA22FF),
        icon = Icons.Default.SportsGymnastics
    )

    val WarriorGold = FitnessColorScheme(
        startColor = Color(0xFFFFE000),
        middleColor = Color(0xFFFF6B6B),
        endColor = Color(0xFFFFAA00),
        icon = Icons.Default.FitnessCenter
    )
}

object MotivationalQuotes {
    private val quotes = listOf(
        MotivationalQuote(
            "El único mal entrenamiento es el que no se hizo.",
            "Anónimo",
            FitnessColors.EnergyRed
        ),
        MotivationalQuote(
            "Tu cuerpo puede soportar casi cualquier cosa. Es tu mente la que debes convencer.",
            "Anónimo",
            FitnessColors.PowerPurple
        ),
        MotivationalQuote(
            "No se trata de tener tiempo, se trata de hacer tiempo.",
            "Anónimo",
            FitnessColors.StrengthOrange
        ),
        MotivationalQuote(
            "La motivación es lo que te pone en marcha. El hábito es lo que te mantiene en movimiento.",
            "Jim Ryun",
            FitnessColors.VitalityGreen
        ),
        MotivationalQuote(
            "El dolor que sientes hoy será la fuerza que sientas mañana.",
            "Anónimo",
            FitnessColors.FocusBlue
        ),
        MotivationalQuote(
            "Los campeones no se hacen en los gimnasios. Los campeones se hacen de algo que tienen muy dentro de ellos: un deseo, un sueño, una visión.",
            "Muhammad Ali",
            FitnessColors.MotivationPink
        ),
        MotivationalQuote(
            "El éxito no es accidental. Es trabajo duro, perseverancia, aprendizaje, estudio, sacrificio y sobre todo, amor por lo que estás haciendo.",
            "Pelé",
            FitnessColors.DeterminationTeal
        ),
        MotivationalQuote(
            "No cuentes los días, haz que los días cuenten.",
            "Muhammad Ali",
            FitnessColors.PassionMagenta
        ),
        MotivationalQuote(
            "La fuerza no viene de ganar. Tus luchas desarrollan tus fortalezas.",
            "Arnold Schwarzenegger",
            FitnessColors.EnduranceIndigo
        ),
        MotivationalQuote(
            "Cuida tu cuerpo. Es el único lugar que tienes para vivir.",
            "Jim Rohn",
            FitnessColors.WarriorGold
        ),
        MotivationalQuote(
            "Lo difícil y lo correcto suelen ser lo mismo.",
            "Anónimo",
            FitnessColors.EnergyRed
        ),
        MotivationalQuote(
            "El fitness no es solo un objetivo, es un estilo de vida.",
            "Anónimo",
            FitnessColors.VitalityGreen
        ),
        MotivationalQuote(
            "Entrena como una bestia, luce como una belleza.",
            "Anónimo",
            FitnessColors.PowerPurple
        ),
        MotivationalQuote(
            "No pares cuando estés cansado. Para cuando hayas terminado.",
            "Anónimo",
            FitnessColors.StrengthOrange
        ),
        MotivationalQuote(
            "La transformación del cuerpo comienza en la mente.",
            "Anónimo",
            FitnessColors.FocusBlue
        )
    )

    /**
     * Obtiene la frase del día basada en las 6:00 AM.
     * La frase cambia automáticamente a las 6:00 AM de cada día.
     */
    fun getQuoteOfTheDay(): MotivationalQuote {
        val now = try {
            LocalDateTime.now()
        } catch (e: Exception) {
            // Fallback si LocalDateTime no está disponible
            val daysSinceEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            val index = (daysSinceEpoch % quotes.size).toInt()
            return quotes[index]
        }

        // Calcular el "día motivacional" basado en ciclos de 6 AM
        // Si la hora actual es antes de las 6 AM, usar el día anterior
        val motivationalDay = if (now.toLocalTime().isBefore(LocalTime.of(6, 0))) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }

        // Usar el número de días desde una fecha fija (epoch) para rotación consistente
        val epochDate = LocalDate.of(2024, 1, 1)
        val daysSinceEpoch = ChronoUnit.DAYS.between(epochDate, motivationalDay)
        val index = (daysSinceEpoch % quotes.size).toInt()

        return quotes[index]
    }

    /**
     * Calcula cuántos milisegundos faltan hasta las próximas 6:00 AM
     */
    fun getMillisUntilNext6AM(): Long {
        val now = try {
            LocalDateTime.now()
        } catch (e: Exception) {
            return 3600000L // 1 hora como fallback
        }

        val next6AM = if (now.toLocalTime().isBefore(LocalTime.of(6, 0))) {
            // Si es antes de las 6 AM hoy, las próximas 6 AM son hoy
            now.toLocalDate().atTime(6, 0)
        } else {
            // Si ya pasaron las 6 AM, las próximas son mañana
            now.toLocalDate().plusDays(1).atTime(6, 0)
        }

        return ChronoUnit.MILLIS.between(now, next6AM)
    }
}

@Composable
fun MotivationalCard(
    modifier: Modifier = Modifier
) {
    // Estado que forzará la recomposición cuando cambie la frase
    var quoteKey by remember { mutableStateOf(0) }

    // Obtener la frase del día
    val quote = remember(quoteKey) {
        MotivationalQuotes.getQuoteOfTheDay()
    }

    // Efecto que monitorea el cambio de frase a las 6:00 AM
    LaunchedEffect(Unit) {
        while (true) {
            // Calcular tiempo hasta las próximas 6 AM
            val millisUntil6AM = MotivationalQuotes.getMillisUntilNext6AM()

            // Esperar hasta las 6 AM
            delay(millisUntil6AM)

            // Actualizar la frase
            quoteKey++

            // Pequeño delay para asegurar que pasó el minuto
            delay(60000L) // 1 minuto
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fondo con gradiente personalizado de fitness
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                quote.colorScheme.startColor,
                                quote.colorScheme.endColor
                            )
                        )
                    )
            )

            // Icono decorativo único en la esquina
            Icon(
                imageVector = quote.colorScheme.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 20.dp),
                tint = Color.White.copy(alpha = 0.1f)
            )

            // Contenido optimizado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge compacto "Frase del DÍA"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "💪",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "FRASE DEL DÍA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "\"${quote.quote}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// Versión compacta para usar en otras pantallas
@Composable
fun CompactMotivationalCard(
    modifier: Modifier = Modifier
) {
    // Estado que forzará la recomposición cuando cambie la frase
    var quoteKey by remember { mutableStateOf(0) }

    // Obtener la frase del día
    val quote = remember(quoteKey) {
        MotivationalQuotes.getQuoteOfTheDay()
    }

    // Efecto que monitorea el cambio de frase a las 6:00 AM
    LaunchedEffect(Unit) {
        while (true) {
            // Calcular tiempo hasta las próximas 6 AM
            val millisUntil6AM = MotivationalQuotes.getMillisUntilNext6AM()

            // Esperar hasta las 6 AM
            delay(millisUntil6AM)

            // Actualizar la frase
            quoteKey++

            // Pequeño delay para asegurar que pasó el minuto
            delay(60000L) // 1 minuto
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gradiente de fondo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                quote.colorScheme.startColor,
                                quote.colorScheme.endColor
                            )
                        )
                    )
            )

            // Icono decorativo pequeño
            Icon(
                imageVector = quote.colorScheme.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp),
                tint = Color.White.copy(alpha = 0.12f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💪",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "MOTIVACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "\"${quote.quote}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
