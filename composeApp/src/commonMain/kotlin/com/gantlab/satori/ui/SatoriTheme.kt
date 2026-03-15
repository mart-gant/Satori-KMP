package com.gantlab.satori.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val HighContrastLightColors = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    outline = Color.Black
)

private val HighContrastDarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    outline = Color.White
)

@Composable
fun SatoriTheme(
    highContrast: Boolean = false,
    largeFont: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColors
        highContrast && !darkTheme -> HighContrastLightColors
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val typography = if (largeFont) {
        Typography(
            bodyLarge = TextStyle(fontSize = 22.sp),
            bodyMedium = TextStyle(fontSize = 20.sp),
            headlineLarge = TextStyle(fontSize = 36.sp),
            titleMedium = TextStyle(fontSize = 24.sp)
        )
    } else {
        Typography()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
