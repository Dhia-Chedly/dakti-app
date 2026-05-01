package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.dakti.app.ui.theme.DaktiThemeTokens

@Composable
fun SplashScreen() {
    val hero = DaktiThemeTokens.hero
    var reveal by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.9f,
        animationSpec = tween(durationMillis = 700),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        reveal = true
    }

    SunsetStadiumBackground(gradientIndex = 0) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alpha)
                .scale(scale)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SportsEmoteRow(
                primary = "⚽",
                secondary = "🏀"
            )
            Text(
                text = "Dakti",
                style = MaterialTheme.typography.displaySmall,
                color = hero.onHero,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Your sports planning hub",
                style = MaterialTheme.typography.bodyMedium,
                color = hero.onHeroMuted,
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
            )
            CircularProgressIndicator(
                color = hero.onHero,
                trackColor = hero.onHero.copy(alpha = 0.28f)
            )
        }
    }
}
