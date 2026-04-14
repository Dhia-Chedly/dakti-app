package com.dakti.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DaktiBlue,
    secondary = DaktiTeal,
    tertiary = DaktiAmber
)

private val DarkColors = darkColorScheme(
    primary = DaktiDarkBlue,
    secondary = DaktiDarkTeal,
    tertiary = DaktiDarkAmber
)

@Composable
fun DaktiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
