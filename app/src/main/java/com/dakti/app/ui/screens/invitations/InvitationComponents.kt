package com.dakti.app.ui.screens.invitations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.invitations.InvitationItemUi
import com.dakti.app.presentation.invitations.PlayerSelectableItemUi
import com.dakti.app.ui.components.AppStateCard

@Composable
fun InvitationStatusChip(statusLabel: String) {
    val colors = when (statusLabel.lowercase()) {
        "accepted" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        "declined" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )

        else -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }

    AssistChip(
        onClick = {},
        enabled = false,
        colors = colors,
        label = { Text(text = statusLabel) }
    )
}

@Composable
fun InvitationCard(
    invitation: InvitationItemUi,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = invitation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = invitation.subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = invitation.scheduledLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = invitation.organizerLabel,
                style = MaterialTheme.typography.bodySmall
            )
            InvitationStatusChip(statusLabel = invitation.statusLabel)
            invitation.message?.takeIf { message -> message.isNotBlank() }?.let { message ->
                Text(
                    text = "Message: $message",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Sent: ${invitation.sentAtLabel}",
                style = MaterialTheme.typography.bodySmall
            )
            invitation.respondedAtLabel?.let { respondedAt ->
                Text(
                    text = "Responded: $respondedAt",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (invitation.canRespond) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAccept(invitation.invitationId) },
                        enabled = !invitation.isResponding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Accept")
                    }
                    OutlinedButton(
                        onClick = { onDecline(invitation.invitationId) },
                        enabled = !invitation.isResponding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Decline")
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerSelectableItem(
    item: PlayerSelectableItemUi,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !item.isAlreadyInvited) { onToggle(item.playerId) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = {
                    if (!item.isAlreadyInvited) {
                        onToggle(item.playerId)
                    }
                },
                enabled = !item.isAlreadyInvited
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.email,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Sport: ${item.preferredSport}",
                    style = MaterialTheme.typography.bodySmall
                )
                item.skillLevel?.takeIf { skill -> skill.isNotBlank() }?.let { skill ->
                    Text(
                        text = "Skill: $skill",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item.availabilityNote?.takeIf { note -> note.isNotBlank() }?.let { note ->
                    Text(
                        text = "Availability: $note",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (item.isAlreadyInvited) {
                    Text(
                        text = "Already invited (${item.existingStatusLabel ?: "Pending"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipationSummaryCard(
    requiredPlayers: Int,
    confirmedPlayers: Int,
    pendingPlayers: Int,
    declinedPlayers: Int,
    remainingSpots: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Participation Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = "Required players: $requiredPlayers", style = MaterialTheme.typography.bodySmall)
            Text(text = "Confirmed: $confirmedPlayers", style = MaterialTheme.typography.bodySmall)
            Text(text = "Pending: $pendingPlayers", style = MaterialTheme.typography.bodySmall)
            Text(text = "Declined: $declinedPlayers", style = MaterialTheme.typography.bodySmall)
            Text(text = "Remaining spots: $remainingSpots", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InvitePlayersEmptyState(
    title: String,
    message: String
) {
    AppStateCard(
        title = title,
        message = message
    )
}

@Composable
fun InvitationsEmptyState(
    title: String,
    message: String
) {
    AppStateCard(
        title = title,
        message = message
    )
}
