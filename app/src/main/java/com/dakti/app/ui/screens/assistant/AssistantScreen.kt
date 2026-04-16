@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.assistant.AssistantMessageUi
import com.dakti.app.ui.components.SectionHeader

@Composable
fun AssistantScreen(
    isLoading: Boolean,
    suggestedPrompts: List<String>,
    messages: List<AssistantMessageUi>,
    onAskSuggestion: (String) -> Unit,
    onGoToInvitations: () -> Unit
) {
    val fallbackPrompt = "Suggest a balanced football match format for 10 players"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Assistant") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "Dakti Assistant",
                    subtitle = "Use quick prompts to generate match planning suggestions."
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Quick prompts",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedPrompts.take(2).forEach { prompt ->
                                AssistChip(
                                    onClick = { onAskSuggestion(prompt) },
                                    label = {
                                        val label = if (prompt.length > 22) {
                                            prompt.take(22) + "..."
                                        } else {
                                            prompt
                                        }
                                        Text(label)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Conversation",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            items(messages.takeLast(6)) { message ->
                MessageBubble(message = message)
            }

            item {
                Button(
                    onClick = {
                        onAskSuggestion(
                            suggestedPrompts.firstOrNull() ?: fallbackPrompt
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = "Generate New Suggestion")
                    }
                }
            }

            item {
                TextButton(
                    onClick = onGoToInvitations,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Open Invitations")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: AssistantMessageUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (message.isFromUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
                .padding(12.dp)
        )
    }
}

