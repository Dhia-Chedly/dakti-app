package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.components.DaktiPlaceholderContent

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onContinueWithoutAuth: () -> Unit
) {
    Scaffold { innerPadding ->
        DaktiPlaceholderContent(
            title = "Welcome to Dakti",
            description = "Phase 1 navigation shell for sports venue booking, matches, and AI assistance.",
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onLoginClick) {
                Text(text = "Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onRegisterClick) {
                Text(text = "Register")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onContinueWithoutAuth) {
                Text(text = "Continue to App")
            }
        }
    }
}
