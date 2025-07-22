package com.example.mirandascloset.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    primaryContainer = Purple40,
    secondary = Color(0xFFFFFFFF),
    tertiary = Tan,
    background = Color(0xFF000000),
    surface = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Brown,
    primaryContainer = Tan,
    secondary = Brown,
    tertiary = Tan,
    background = Color(0xFFEEEEEE),
    surface = Color(0xFFEEEEEE)
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