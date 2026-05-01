package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.dakti.app.presentation.auth.OnboardingStage
import com.dakti.app.ui.theme.DaktiThemeTokens
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    stages: List<OnboardingStage>,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val hero = DaktiThemeTokens.hero
    val pagerState = rememberPagerState(pageCount = { stages.size })
    val scope = rememberCoroutineScope()

    SunsetStadiumBackground(gradientIndex = pagerState.currentPage) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < stages.lastIndex) {
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Skip",
                            color = hero.onHero,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(top = 26.dp, bottom = 140.dp)
                    .testTag("onboarding_pager")
            ) { page ->
                val stage = stages[page]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SportsEmoteRow(
                        primary = stage.primaryEmote,
                        secondary = stage.secondaryEmote
                    )
                    StageIcon(kind = stage.kind)
                    Text(
                        text = stage.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = hero.onHero,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stage.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = hero.onHeroMuted,
                        textAlign = TextAlign.Center
                    )
                    androidx.compose.material3.Icon(
                        imageVector = onboardingAccentIcon,
                        contentDescription = null,
                        tint = hero.onHero.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(stages.size) { index ->
                        val selected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(width = if (selected) 28.dp else 8.dp, height = 8.dp)
                                .background(
                                    color = if (selected) hero.onHero else hero.onHero.copy(alpha = 0.38f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == stages.lastIndex) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (pagerState.currentPage == stages.lastIndex) {
                            "Get Started"
                        } else {
                            "Next"
                        }
                    )
                }
            }
        }
    }
}
