@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.assistant.AssistantQuickActionUi
import com.dakti.app.presentation.assistant.AssistantUiState

@Composable
fun AssistantScreen(
    uiState: AssistantUiState,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onPromptSelected: (String) -> Unit,
    onQuickActionSelected: (AssistantQuickActionUi) -> Unit,
    onUseVenueSuggestion: (messageId: String, suggestionId: String) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onDismissActionResult: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Assistant") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "AI Organizer Assistant",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.quickActions, key = { action -> action.id }) { action ->
                            AssistantQuickActionChip(
                                action = action,
                                onClick = onQuickActionSelected
                            )
                        }
                    }
                }

                if (uiState.showWelcomeState) {
                    item {
                        AssistantEmptyState()
                    }

                    if (uiState.suggestedPrompts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Try one of these prompts",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        items(uiState.suggestedPrompts, key = { prompt -> prompt }) { prompt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                TextButton(
                                    onClick = { onPromptSelected(prompt) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                items(uiState.messages, key = { message -> message.id }) { message ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChatMessageBubble(message = message)

                        if (message.suggestions.isNotEmpty()) {
                            message.suggestions.forEach { suggestion ->
                                AssistantSuggestionCard(suggestion = suggestion)
                            }
                        }

                        if (message.venueSuggestions.isNotEmpty()) {
                            message.venueSuggestions.forEach { venueSuggestion ->
                                AssistantVenueSuggestionCard(
                                    suggestion = venueSuggestion,
                                    onUseThisOption = { suggestionId ->
                                        onUseVenueSuggestion(message.id, suggestionId)
                                    }
                                )
                            }
                        }

                        message.generatedMessage?.let { generatedMessage ->
                            AssistantGeneratedMessageCard(generated = generatedMessage)
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        AssistantLoadingMessage()
                    }
                }
            }

            uiState.pendingActionProposal?.let { proposal ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistantActionProposalCard(
                        proposal = proposal,
                        isExecuting = uiState.isExecutingAction,
                        onConfirm = onConfirmAction,
                        onCancel = onCancelAction
                    )
                }
            }

            uiState.actionResultMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = onDismissActionResult,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = "Dismiss")
                        }
                    }
                }
            }

            uiState.errorMessage?.takeIf { message -> message.isNotBlank() }?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
                                Text(text = "Retry")
                            }
                            TextButton(onClick = onDismissError, modifier = Modifier.weight(1f)) {
                                Text(text = "Dismiss")
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = onInputChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = "Ask assistant to organize, suggest, or draft...") },
                    maxLines = 4
                )
                FilledIconButton(
                    onClick = onSendMessage,
                    enabled = uiState.canSend
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

