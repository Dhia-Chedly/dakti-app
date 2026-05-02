package com.dakti.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
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
data class DaktiChromeColors(
    val container: Color,
    val border: Color,
    val content: Color,
    val selectedPill: Color,
    val selectedContent: Color
)

@Immutable
data class DaktiBackdropScrimColors(
    val strong: Color,
    val medium: Color,
    val light: Color
)

@Immutable
data class DaktiChromeMetrics(
    val cornerRadius: Dp = 20.dp,
    val horizontalMargin: Dp = 12.dp,
    val verticalInset: Dp = 8.dp
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
private val LocalDaktiChromeMetrics = staticCompositionLocalOf { DaktiChromeMetrics() }
private val LocalDaktiHeroColors = staticCompositionLocalOf {
    DaktiHeroColors(
        top = Color(0xFFEEF5FF),
        middle = Color(0xFFE3EEFF),
        bottom = Color(0xFFD7E7FF),
        glow = DaktiLimeSoft,
        onHero = DaktiOnBackground,
        onHeroMuted = DaktiOnSurfaceVariant,
        glassStrong = Color.White.copy(alpha = 0.95f),
        glassSoft = Color.White.copy(alpha = 0.5f)
    )
}
private val LocalDaktiChromeColors = staticCompositionLocalOf {
    DaktiChromeColors(
        container = Color.White.copy(alpha = 0.64f),
        border = Color.White.copy(alpha = 0.62f),
        content = DaktiOnBackground,
        selectedPill = DaktiPrimaryContainer.copy(alpha = 0.9f),
        selectedContent = DaktiOnPrimaryContainer
    )
}
private val LocalDaktiBackdropScrims = staticCompositionLocalOf {
    DaktiBackdropScrimColors(
        strong = Color(0xCCEAF2FF),
        medium = Color(0x99E2ECFF),
        light = Color(0x66DAE6FF)
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

    val chromeMetrics: DaktiChromeMetrics
        @Composable get() = LocalDaktiChromeMetrics.current

    val hero: DaktiHeroColors
        @Composable get() = LocalDaktiHeroColors.current

    val chrome: DaktiChromeColors
        @Composable get() = LocalDaktiChromeColors.current

    val backgroundScrim: DaktiBackdropScrimColors
        @Composable get() = LocalDaktiBackdropScrims.current

    val semantic: DaktiSemanticColors
        @Composable get() = LocalDaktiSemanticColors.current

    val isDarkTheme: Boolean
        @Composable get() = LocalIsDarkTheme.current
}

private fun heroColorsFor(darkTheme: Boolean): DaktiHeroColors {
    return if (darkTheme) {
        DaktiHeroColors(
            top = Color(0xFF3B5474),
            middle = Color(0xFF446183),
            bottom = Color(0xFF4F6F92),
            glow = DaktiLimeSoft,
            onHero = Color(0xFFEAF2FF),
            onHeroMuted = Color(0xFFD9E6F8),
            glassStrong = Color(0xD9435F82),
            glassSoft = Color(0x754E6D92)
        )
    } else {
        DaktiHeroColors(
            top = Color(0xFFEEF5FF),
            middle = Color(0xFFE3EEFF),
            bottom = Color(0xFFD7E7FF),
            glow = DaktiLimeSoft,
            onHero = DaktiOnBackground,
            onHeroMuted = DaktiOnSurfaceVariant,
            glassStrong = Color.White.copy(alpha = 0.95f),
            glassSoft = Color.White.copy(alpha = 0.58f)
        )
    }
}

private fun chromeColorsFor(darkTheme: Boolean): DaktiChromeColors {
    return if (darkTheme) {
        DaktiChromeColors(
            container = Color(0xB237506F),
            border = Color(0x80BFD7F4),
            content = Color(0xFFE6F0FF),
            selectedPill = Color(0xFF9CB9DB),
            selectedContent = Color(0xFF10263F)
        )
    } else {
        DaktiChromeColors(
            container = Color.White.copy(alpha = 0.64f),
            border = Color.White.copy(alpha = 0.62f),
            content = DaktiOnBackground,
            selectedPill = DaktiPrimaryContainer.copy(alpha = 0.9f),
            selectedContent = DaktiOnPrimaryContainer
        )
    }
}

private fun scrimColorsFor(darkTheme: Boolean): DaktiBackdropScrimColors {
    return if (darkTheme) {
        DaktiBackdropScrimColors(
            strong = Color(0xB2233550),
            medium = Color(0x8A2B3F5E),
            light = Color(0x66344B6D)
        )
    } else {
        DaktiBackdropScrimColors(
            strong = Color(0xCCEAF2FF),
            medium = Color(0x99E2ECFF),
            light = Color(0x66DAE6FF)
        )
    }
}

@Composable
fun DaktiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDaktiDimensions provides DaktiDimensions(),
        LocalDaktiSpacing provides DaktiSpacing(),
        LocalDaktiElevations provides DaktiElevations(),
        LocalDaktiChromeMetrics provides DaktiChromeMetrics(),
        LocalDaktiHeroColors provides heroColorsFor(darkTheme),
        LocalDaktiChromeColors provides chromeColorsFor(darkTheme),
        LocalDaktiBackdropScrims provides scrimColorsFor(darkTheme),
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
