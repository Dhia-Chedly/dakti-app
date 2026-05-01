package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.theme.DaktiThemeTokens

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val hero = DaktiThemeTokens.hero
    SunsetStadiumBackground(gradientIndex = 1) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SportsEmoteRow(
                    primary = "?",
                    secondary = "??"
                )
                Text(
                    text = "Dakti",
                    style = MaterialTheme.typography.displaySmall,
                    color = hero.onHero,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    text = "Plan games, reserve venues, and keep your squad ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = hero.onHeroMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            AuthGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Start by logging in, or create an account to unlock match coordination tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                ) {
                    Text(text = "Login")
                }

                OutlinedButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(text = "Create Account")
                }
            }
        }
    }
}
