package com.dakti.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

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

private val DaktiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp)
)

@Composable
fun DaktiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = DaktiShapes,
        content = content
    )
}
