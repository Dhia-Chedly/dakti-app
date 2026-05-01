package com.dakti.app.ui.screens.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dakti.app.domain.model.AssistantGeneratedMessageKind
import com.dakti.app.domain.model.AssistantMessageRole
import com.dakti.app.presentation.assistant.AssistantActionProposalUi
import com.dakti.app.presentation.assistant.AssistantGeneratedMessageUi
import com.dakti.app.presentation.assistant.AssistantMessageUi
import com.dakti.app.presentation.assistant.AssistantQuickActionUi
import com.dakti.app.presentation.assistant.AssistantSuggestionUi
import com.dakti.app.presentation.assistant.AssistantVenueSuggestionUi

@Composable
fun ChatMessageBubble(message: AssistantMessageUi) {
    val isUser = message.role == AssistantMessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (message.isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            message.intentLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium
            )
            message.providerLabel?.takeIf { label -> label.isNotBlank() }?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = message.timestampLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AssistantQuickActionChip(
    action: AssistantQuickActionUi,
    onClick: (AssistantQuickActionUi) -> Unit
) {
    AssistChip(
        onClick = { onClick(action) },
        label = { Text(text = action.title) }
    )
}

@Composable
fun AssistantSuggestionCard(suggestion: AssistantSuggestionUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            suggestion.description?.takeIf { description -> description.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = suggestion.typeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AssistantVenueSuggestionCard(
    suggestion: AssistantVenueSuggestionUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = suggestion.venueName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = suggestion.timeSlotLabel,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = suggestion.venueAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.bodySmall
            )
            suggestion.slotCapacity?.let { capacity ->
                Text(
                    text = "Slot capacity: $capacity",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (suggestion.isPreferredTime) {
                Text(
                    text = "Matches preferred time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AssistantGeneratedMessageCard(
    generated: AssistantGeneratedMessageUi,
    onCopy: () -> Unit,
    onSendViaWhatsApp: () -> Unit,
    onSendViaEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = generated.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = when (generated.kind) {
                    AssistantGeneratedMessageKind.INVITATION -> "Invitation Content"
                    AssistantGeneratedMessageKind.REMINDER -> "Reminder Content"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = generated.content,
                style = MaterialTheme.typography.bodySmall
            )
            if (generated.variants.isNotEmpty()) {
                Text(
                    text = "Variants",
                    style = MaterialTheme.typography.labelMedium
                )
                generated.variants.forEach { variant ->
                    Text(
                        text = "- $variant",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Copy")
                }
                OutlinedButton(
                    onClick = onSendViaWhatsApp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "WhatsApp")
                }
            }
            OutlinedButton(
                onClick = onSendViaEmail,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send via Email")
            }
        }
    }
}

@Composable
fun AssistantActionProposalCard(
    proposal: AssistantActionProposalUi,
    isExecuting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = proposal.summary,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    enabled = !isExecuting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (isExecuting) "Executing..." else "Confirm")
                }
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isExecuting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Composable
fun AssistantEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Hi, I'm Dakti Assistant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "I can chat about available venues and answer practical sports questions like gear, warm-up, and preparation.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AssistantLoadingMessage() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Text(
                text = "Assistant is analyzing your request...",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
