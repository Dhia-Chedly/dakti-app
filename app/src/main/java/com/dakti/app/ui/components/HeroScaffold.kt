package com.dakti.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
    ) {
        DaktiHeroBackdrop()
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            containerColor = containerColor,
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to hero.top,
                        0.26f to hero.middle,
                        0.48f to hero.bottom.copy(alpha = 0.35f),
                        0.66f to Color.Transparent
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(190.dp)
                .clip(CircleShape)
                .background(hero.glow.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(160.dp)
                .clip(CircleShape)
                .background(hero.glassSoft)
        )
        content()
    }
}
