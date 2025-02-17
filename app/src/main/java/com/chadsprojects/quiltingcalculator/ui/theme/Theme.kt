package com.chadsprojects.quiltingcalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF880E4F),
    secondary = Color(0xFFD81B60),
    background = Color(0xFF880E4F), // Keep a solid background
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF99C8),
    secondary = Color(0xFFD81B60),
    background = Color(0x66F06292),
    onPrimary = Color.Black
)

@Composable
fun QuiltingCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
