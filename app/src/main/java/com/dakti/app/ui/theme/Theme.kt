package com.dakti.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = DaktiPrimary,
    onPrimary = DaktiOnPrimary,
    primaryContainer = DaktiPrimaryContainer,
    onPrimaryContainer = DaktiOnPrimaryContainer,
    secondary = DaktiSecondary,
    onSecondary = DaktiOnSecondary,
    secondaryContainer = DaktiSecondaryContainer,
    onSecondaryContainer = DaktiOnSecondaryContainer,
    tertiary = DaktiTertiary,
    onTertiary = DaktiOnTertiary,
    tertiaryContainer = DaktiTertiaryContainer,
    onTertiaryContainer = DaktiOnTertiaryContainer,
    background = DaktiBackground,
    onBackground = DaktiOnBackground,
    surface = DaktiSurface,
    onSurface = DaktiOnSurface,
    surfaceVariant = DaktiSurfaceVariant,
    onSurfaceVariant = DaktiOnSurfaceVariant,
    outline = DaktiOutline,
    outlineVariant = DaktiOutlineVariant,
    error = DaktiError,
    onError = DaktiOnError,
    errorContainer = DaktiErrorContainer,
    onErrorContainer = DaktiOnErrorContainer,
    surfaceContainer = DaktiSurfaceContainer,
    surfaceContainerHigh = DaktiSurfaceContainerHigh,
    surfaceContainerHighest = DaktiSurfaceContainerHighest,
    surfaceContainerLow = DaktiSurfaceContainerLow,
    surfaceContainerLowest = DaktiSurfaceContainerLowest
)

private val DarkColors = darkColorScheme(
    primary = DaktiDarkPrimary,
    onPrimary = DaktiDarkOnPrimary,
    primaryContainer = DaktiDarkPrimaryContainer,
    onPrimaryContainer = DaktiDarkOnPrimaryContainer,
    secondary = DaktiDarkSecondary,
    onSecondary = DaktiDarkOnSecondary,
    secondaryContainer = DaktiDarkSecondaryContainer,
    onSecondaryContainer = DaktiDarkOnSecondaryContainer,
    tertiary = DaktiDarkTertiary,
    onTertiary = DaktiDarkOnTertiary,
    tertiaryContainer = DaktiDarkTertiaryContainer,
    onTertiaryContainer = DaktiDarkOnTertiaryContainer,
    background = DaktiDarkBackground,
    onBackground = DaktiDarkOnBackground,
    surface = DaktiDarkSurface,
    onSurface = DaktiDarkOnSurface,
    surfaceVariant = DaktiDarkSurfaceVariant,
    onSurfaceVariant = DaktiDarkOnSurfaceVariant,
    outline = DaktiDarkOutline,
    outlineVariant = DaktiDarkOutlineVariant,
    error = DaktiDarkError,
    onError = DaktiDarkOnError,
    errorContainer = DaktiDarkErrorContainer,
    onErrorContainer = DaktiDarkOnErrorContainer,
    surfaceContainer = DaktiDarkSurfaceContainer,
    surfaceContainerHigh = DaktiDarkSurfaceContainerHigh,
    surfaceContainerHighest = DaktiDarkSurfaceContainerHighest,
    surfaceContainerLow = DaktiDarkSurfaceContainerLow,
    surfaceContainerLowest = DaktiDarkSurfaceContainerLowest
)

private val DaktiShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Immutable
data class DaktiDimensions(
    val screenHorizontal: Dp = 24.dp,
    val sectionSpacing: Dp = 20.dp,
    val cardSpacing: Dp = 12.dp,
    val iconTouchTarget: Dp = 48.dp
)

@Immutable
data class DaktiSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp
)

@Immutable
data class DaktiElevations(
    val low: Dp = 1.dp,
    val medium: Dp = 3.dp,
    val high: Dp = 6.dp
)

@Immutable
data class DaktiHeroColors(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val glow: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    val glassStrong: Color,
    val glassSoft: Color
)

@Immutable
data class DaktiSemanticColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color
)

private val LocalDaktiDimensions = staticCompositionLocalOf { DaktiDimensions() }
private val LocalDaktiSpacing = staticCompositionLocalOf { DaktiSpacing() }
private val LocalDaktiElevations = staticCompositionLocalOf { DaktiElevations() }
private val LocalDaktiHeroColors = staticCompositionLocalOf {
    DaktiHeroColors(
        top = DaktiNavy,
        middle = DaktiNavyMid,
        bottom = DaktiNavyAlt,
        glow = DaktiLimeSoft,
        onHero = Color.White,
        onHeroMuted = Color.White.copy(alpha = 0.9f),
        glassStrong = Color.White.copy(alpha = 0.9f),
        glassSoft = Color.White.copy(alpha = 0.18f)
    )
}
private val LocalDaktiSemanticColors = staticCompositionLocalOf {
    DaktiSemanticColors(
        success = DaktiSuccess,
        warning = DaktiWarning,
        danger = DaktiDanger,
        info = DaktiInfo
    )
}
private val LocalIsDarkTheme = staticCompositionLocalOf { false }

object DaktiThemeTokens {
    val dimensions: DaktiDimensions
        @Composable get() = LocalDaktiDimensions.current

    val spacing: DaktiSpacing
        @Composable get() = LocalDaktiSpacing.current

    val elevations: DaktiElevations
        @Composable get() = LocalDaktiElevations.current

    val hero: DaktiHeroColors
        @Composable get() = LocalDaktiHeroColors.current

    val semantic: DaktiSemanticColors
        @Composable get() = LocalDaktiSemanticColors.current

    val isDarkTheme: Boolean
        @Composable get() = LocalIsDarkTheme.current
}

private fun heroColorsFor(darkTheme: Boolean): DaktiHeroColors {
    return if (darkTheme) {
        DaktiHeroColors(
            top = Color(0xFF050F1E),
            middle = Color(0xFF0A1B34),
            bottom = Color(0xFF12335C),
            glow = DaktiLime,
            onHero = Color(0xFFEAF2FF),
            onHeroMuted = Color(0xFFD3E3FF),
            glassStrong = Color(0xCC112947),
            glassSoft = Color(0x661D3B62)
        )
    } else {
        DaktiHeroColors(
            top = Color(0xFF0A1B34),
            middle = Color(0xFF12335C),
            bottom = Color(0xFF1A4F82),
            glow = DaktiLimeSoft,
            onHero = Color.White,
            onHeroMuted = Color.White.copy(alpha = 0.9f),
            glassStrong = Color.White.copy(alpha = 0.92f),
            glassSoft = Color.White.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun DaktiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDaktiDimensions provides DaktiDimensions(),
        LocalDaktiSpacing provides DaktiSpacing(),
        LocalDaktiElevations provides DaktiElevations(),
        LocalDaktiHeroColors provides heroColorsFor(darkTheme),
        LocalDaktiSemanticColors provides DaktiSemanticColors(
            success = DaktiSuccess,
            warning = DaktiWarning,
            danger = DaktiDanger,
            info = DaktiInfo
        ),
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = DaktiShapes,
            content = content
        )
    }
}
