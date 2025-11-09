package com.example.trailblazer.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TrailGreen,
    onPrimary = Color.White,
    primaryContainer = TrailGreenLight,
    onPrimaryContainer = Color(0xFF1B5E20),

    secondary = TrailBlue,
    onSecondary = Color.White,

    tertiary = TrailOrange,
    onTertiary = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,

    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = TextSecondary,

    error = Color(0xFFD32F2F),
    onError = Color.White,
)

@Composable
fun TrailblazerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}