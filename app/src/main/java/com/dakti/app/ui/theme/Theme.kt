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
    val low: Dp = 2.dp,
    val medium: Dp = 6.dp,
    val high: Dp = 10.dp
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
    val selectedContent: Color,
    val shadowAmbient: Color,
    val shadowSpot: Color
)

@Immutable
data class DaktiBackdropScrimColors(
    val base: Color,
    val verticalStart: Color,
    val verticalMid: Color,
    val verticalEnd: Color,
    val radialCore: Color,
    val radialMid: Color,
    val radialOuter: Color,
    val orbPrimary: Color,
    val orbSecondary: Color
)

@Immutable
data class DaktiAccentShadowColors(
    val cardBorder: Color,
    val cardBorderStrong: Color,
    val cardShadowAmbient: Color,
    val cardShadowSpot: Color,
    val glowTint: Color
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
private val LocalDaktiHeroColors = staticCompositionLocalOf { heroColorsFor(darkTheme = false) }
private val LocalDaktiChromeColors = staticCompositionLocalOf { chromeColorsFor(darkTheme = false) }
private val LocalDaktiBackdropScrims = staticCompositionLocalOf { scrimColorsFor(darkTheme = false) }
private val LocalDaktiAccentShadows = staticCompositionLocalOf { accentShadowsFor(darkTheme = false) }
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

    val accentShadows: DaktiAccentShadowColors
        @Composable get() = LocalDaktiAccentShadows.current

    val semantic: DaktiSemanticColors
        @Composable get() = LocalDaktiSemanticColors.current

    val isDarkTheme: Boolean
        @Composable get() = LocalIsDarkTheme.current
}

private fun heroColorsFor(darkTheme: Boolean): DaktiHeroColors {
    return if (darkTheme) {
        DaktiHeroColors(
            top = Color(0xFF0A110D),
            middle = Color(0xFF101A14),
            bottom = Color(0xFF132119),
            glow = Color(0xFF55C896),
            onHero = DaktiDarkOnBackground,
            onHeroMuted = DaktiDarkOnSurfaceVariant,
            glassStrong = Color(0xD9192A22),
            glassSoft = Color(0xA015231D)
        )
    } else {
        DaktiHeroColors(
            top = Color(0xFFFFFFFF),
            middle = Color(0xFFF8FFF9),
            bottom = Color(0xFFEEF8F1),
            glow = Color(0xFF56D79F),
            onHero = DaktiOnBackground,
            onHeroMuted = DaktiOnSurfaceVariant,
            glassStrong = Color(0xF3FFFFFF),
            glassSoft = Color(0xCCF0F9F3)
        )
    }
}

private fun chromeColorsFor(darkTheme: Boolean): DaktiChromeColors {
    return if (darkTheme) {
        DaktiChromeColors(
            container = Color(0xD91A2A22),
            border = Color(0x99A0D4B8),
            content = DaktiDarkOnBackground,
            selectedPill = Color(0xFF2C6A50),
            selectedContent = Color(0xFFE6F4EB),
            shadowAmbient = Color(0x6620A56D),
            shadowSpot = Color(0x8C0F3C2D)
        )
    } else {
        DaktiChromeColors(
            container = Color(0xEFFFFFFF),
            border = Color(0xA8A6CDB7),
            content = DaktiOnBackground,
            selectedPill = Color(0xFFBDF2D7),
            selectedContent = Color(0xFF0C3A2A),
            shadowAmbient = Color(0x332E8F66),
            shadowSpot = Color(0x4D296D4F)
        )
    }
}

private fun scrimColorsFor(darkTheme: Boolean): DaktiBackdropScrimColors {
    return if (darkTheme) {
        DaktiBackdropScrimColors(
            base = Color(0xFF090F0C),
            verticalStart = Color(0x4A0E2B1D),
            verticalMid = Color(0x32143024),
            verticalEnd = Color(0x140F1D16),
            radialCore = Color(0x6E1E6E4D),
            radialMid = Color(0x4A1A573E),
            radialOuter = Color(0x1C163026),
            orbPrimary = Color(0x4A2B8A60),
            orbSecondary = Color(0x33216C4D)
        )
    } else {
        DaktiBackdropScrimColors(
            base = Color(0xFFFFFFFF),
            verticalStart = Color(0x26C8F0D6),
            verticalMid = Color(0x16E0F6E8),
            verticalEnd = Color(0x0CF7FBF8),
            radialCore = Color(0x2EA9E9C9),
            radialMid = Color(0x1BC9F0DA),
            radialOuter = Color(0x09E9F7EF),
            orbPrimary = Color(0x26A4EECB),
            orbSecondary = Color(0x19C3F5DB)
        )
    }
}

private fun accentShadowsFor(darkTheme: Boolean): DaktiAccentShadowColors {
    return if (darkTheme) {
        DaktiAccentShadowColors(
            cardBorder = Color(0x99A1D3B7),
            cardBorderStrong = Color(0xCC8CD3AE),
            cardShadowAmbient = Color(0x6638B67F),
            cardShadowSpot = Color(0x8C2A8058),
            glowTint = Color(0x3390E5BB)
        )
    } else {
        DaktiAccentShadowColors(
            cardBorder = Color(0x99A4CDB7),
            cardBorderStrong = Color(0xCCA0D5B9),
            cardShadowAmbient = Color(0x332D9A6A),
            cardShadowSpot = Color(0x52317F5C),
            glowTint = Color(0x3388E7BC)
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
        LocalDaktiAccentShadows provides accentShadowsFor(darkTheme),
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
