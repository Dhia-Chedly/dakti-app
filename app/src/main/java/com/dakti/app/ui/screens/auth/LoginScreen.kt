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
fun LoginScreen(
    message: String,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Login") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Sign in",
            description = message,
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onLoginClick) {
                Text(text = "Demo Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onGoToRegister) {
                Text(text = "Need an account?")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }
    }
}
