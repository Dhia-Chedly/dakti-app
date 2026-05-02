package com.dakti.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dakti.app.R
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
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.sports_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        DaktiBackgroundScrim()
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(190.dp)
                .clip(CircleShape)
                .background(hero.glow.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(160.dp)
                .clip(CircleShape)
                .background(hero.glassSoft.copy(alpha = 0.55f))
        )
        content()
    }
}

@Composable
fun DaktiBackgroundScrim(
    modifier: Modifier = Modifier
) {
    val scrim = DaktiThemeTokens.backgroundScrim

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to scrim.strong,
                        0.26f to scrim.medium,
                        0.56f to scrim.light,
                        0.8f to Color.Transparent
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
                        scrim.strong.copy(alpha = 0.92f),
                        scrim.medium.copy(alpha = 0.7f),
                        scrim.light.copy(alpha = 0.44f),
                        Color.Transparent
                    )
                )
            )
    )
}
