package com.dakti.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.theme.DaktiThemeTokens

@Composable
fun daktiCardBorder(
    strong: Boolean = false,
    alphaMultiplier: Float = 1f
): BorderStroke {
    val accent = DaktiThemeTokens.accentShadows
    val baseColor = if (strong) accent.cardBorderStrong else accent.cardBorder
    return BorderStroke(width = 1.dp, color = baseColor.copy(alpha = baseColor.alpha * alphaMultiplier))
}

@Composable
fun Modifier.daktiAccentCard(
    shape: Shape,
    elevation: Dp = 2.dp
): Modifier {
    val accent = DaktiThemeTokens.accentShadows
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = accent.cardShadowAmbient,
            spotColor = accent.cardShadowSpot
        )
}
