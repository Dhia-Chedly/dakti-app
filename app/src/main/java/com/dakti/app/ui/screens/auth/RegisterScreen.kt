@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.components.DaktiPlaceholderContent

@Composable
fun RegisterScreen(
    message: String,
    onRegisterClick: () -> Unit,
    onGoToLogin: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Register") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Create account",
            description = message,
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onRegisterClick) {
                Text(text = "Demo Register")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onGoToLogin) {
                Text(text = "Already have an account?")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }
    }
}
