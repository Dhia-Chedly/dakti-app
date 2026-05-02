package com.dakti.app.ui.screens.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dakti.app.R
import com.dakti.app.presentation.auth.OnboardingStageKind
import com.dakti.app.ui.components.DaktiBackgroundScrim
import com.dakti.app.ui.theme.DaktiThemeTokens

private data class AuthGradientSpec(
    val glow: Color
)

@Composable
private fun gradientSpec(index: Int): AuthGradientSpec {
    val hero = DaktiThemeTokens.hero
    val variantShift = (index.mod(4)) * 0.08f
    return AuthGradientSpec(
        glow = lerp(hero.glow, hero.onHero, variantShift * 0.45f)
    )
}

@Composable
fun SunsetStadiumBackground(
    gradientIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val spec = gradientSpec(gradientIndex)
    val hero = DaktiThemeTokens.hero
    val transition = rememberInfiniteTransition(label = "auth_background")
    val driftX = transition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_x"
    )
    val driftY = transition.animateFloat(
        initialValue = 24f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_y"
    )

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
                .padding(start = (driftX.value + 210f).dp, top = 72.dp)
                .size(170.dp)
                .clip(CircleShape)
                .background(spec.glow.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = (driftY.value + 380f).dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(hero.glassSoft.copy(alpha = 0.58f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 90.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(spec.glow.copy(alpha = 0.14f))
        )
        content()
    }
}

@Composable
fun AuthGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val hero = DaktiThemeTokens.hero
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = hero.glassStrong)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun SportsEmoteBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    val hero = DaktiThemeTokens.hero
    Card(
        modifier = modifier,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = hero.glassSoft)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = hero.onHero,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StageIcon(kind: OnboardingStageKind) {
    val hero = DaktiThemeTokens.hero
    Icon(
        imageVector = kind.toIcon(),
        contentDescription = null,
        tint = hero.onHero,
        modifier = Modifier.size(32.dp)
    )
}

@Composable
fun SportsEmoteRow(
    primary: String,
    secondary: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SportsEmoteBadge(text = primary)
        SportsEmoteBadge(
            text = secondary,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

private fun OnboardingStageKind.toIcon(): ImageVector {
    return when (this) {
        OnboardingStageKind.VENUES -> Icons.Outlined.Stadium
        OnboardingStageKind.MATCHES -> Icons.Outlined.Groups
        OnboardingStageKind.ASSISTANT -> Icons.Outlined.SmartToy
        OnboardingStageKind.READY -> Icons.Outlined.SportsSoccer
    }
}

val onboardingAccentIcon: ImageVector = Icons.Outlined.Place
