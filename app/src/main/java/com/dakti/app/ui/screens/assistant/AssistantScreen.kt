@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.assistant

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.components.DaktiPlaceholderContent

@Composable
fun AssistantScreen(
    lastResponse: String,
    onAskSuggestion: () -> Unit,
    onGoToInvitations: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "AI Assistant") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Dakti Assistant",
            description = lastResponse,
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onAskSuggestion) {
                Text(text = "Ask for Suggestion")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onGoToInvitations) {
                Text(text = "Open Invitations")
            }
        }
    }
}
