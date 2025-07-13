package com.example.mirandascloset.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Tan,
    secondary = Brown,
    tertiary = Tan
)

private val LightColorScheme = lightColorScheme(
    primary = Brown,
    primaryContainer = Tan,
    secondary = Brown,
    tertiary = Tan,
    background = Color(0xFFEEEEEE)
)

@Composable
fun MirandasClosetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}