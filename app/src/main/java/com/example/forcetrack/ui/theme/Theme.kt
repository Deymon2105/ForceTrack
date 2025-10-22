package com.example.forcetrack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ForceTrackColorScheme = darkColorScheme(
    // Colores principales
    primary = ButtonGreen,
    onPrimary = Color.White,
    primaryContainer = ButtonGreen.copy(alpha = 0.3f),
    onPrimaryContainer = ButtonGreen,

    // Colores secundarios
    secondary = InputTextBlue,
    onSecondary = Color.White,
    secondaryContainer = InputTextBlue.copy(alpha = 0.3f),
    onSecondaryContainer = InputTextBlue,

    // Colores de fondo
    background = BackgroundDark,
    onBackground = TextLight,

    // Superficies (Cards, etc)
    surface = InputBackground,
    onSurface = TextLight,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextLight,

    // Otros colores
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.3f),
    onErrorContainer = ErrorRed,

    // Contornos
    outline = TextLight.copy(alpha = 0.3f),
    outlineVariant = TextLight.copy(alpha = 0.15f),

    // Contenedores de superficies
    surfaceContainer = InputBackground,
    surfaceContainerHigh = SurfaceDark,
    surfaceContainerHighest = SurfaceDark,
    surfaceContainerLow = BackgroundDark,
    surfaceContainerLowest = BackgroundDark,

    // Superficie inversa
    inverseSurface = TextLight,
    inverseOnSurface = BackgroundDark,
    inversePrimary = ButtonGreen
)

@Composable
fun ForcetrackTheme(
    darkTheme: Boolean = true, // Siempre usar tema oscuro
    dynamicColor: Boolean = false, // Desactivar colores dinámicos para usar nuestra paleta
    content: @Composable () -> Unit
) {
    val colorScheme = ForceTrackColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = BackgroundDark.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}