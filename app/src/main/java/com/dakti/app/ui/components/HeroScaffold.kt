package com.dakti.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.theme.DaktiThemeTokens

@Composable
fun DaktiHeroScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val backgroundTexture = DaktiThemeTokens.backgroundScrim
    val resolvedContainerColor = if (containerColor == Color.Transparent) {
        MaterialTheme.colorScheme.background
    } else {
        containerColor
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = backgroundTexture.base)
    ) {
        DaktiHeroBackdrop()
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            containerColor = resolvedContainerColor,
            content = content
        )
    }
}

@Composable
fun DaktiHeroBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val hero = DaktiThemeTokens.hero
    val texture = DaktiThemeTokens.backgroundScrim

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(texture.base)
    ) {
        DaktiBackgroundScrim()
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 10.dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(texture.orbPrimary)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 112.dp, start = 8.dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(texture.orbSecondary)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 94.dp, end = 18.dp)
                .size(138.dp)
                .clip(CircleShape)
                .background(hero.glow.copy(alpha = 0.13f))
        )
        content()
    }
}

@Composable
fun DaktiBackgroundScrim(
    modifier: Modifier = Modifier
) {
    val texture = DaktiThemeTokens.backgroundScrim

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to texture.verticalStart,
                        0.32f to texture.verticalMid,
                        0.72f to texture.verticalEnd,
                        1f to Color.Transparent
                    )
                )
            )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        texture.radialCore,
                        texture.radialMid,
                        texture.radialOuter,
                        Color.Transparent
                    )
                )
            )
    )
}
