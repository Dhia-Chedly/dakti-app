@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.invitations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.dakti.app.ui.components.DaktiHeroScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.invitations.InvitePlayersUiState
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.SectionHeader

@Composable
fun InvitePlayersScreen(
    uiState: InvitePlayersUiState,
    onMessageChanged: (String) -> Unit,
    onGenerateAiMessage: () -> Unit,
    onTogglePlayer: (String) -> Unit,
    onSendInvitations: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    DaktiHeroScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Invite Players") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = if (uiState.matchTitle.isBlank()) {
                        "Invite Players"
                    } else {
                        uiState.matchTitle
                    },
                    subtitle = "Select players, add an optional note, and send invitations."
                )
            }

            item {
                Text(
                    text = "${uiState.sportType} - ${uiState.venueName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.scheduledLabel.isNotBlank()) {
                item {
                    Text(
                        text = "Scheduled: ${uiState.scheduledLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                ParticipationSummaryCard(
                    requiredPlayers = uiState.requiredPlayers,
                    confirmedPlayers = uiState.confirmedPlayersCount,
                    pendingPlayers = uiState.pendingPlayersCount,
                    declinedPlayers = uiState.declinedPlayersCount,
                    remainingSpots = uiState.remainingSpots
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.messageInput,
                    onValueChange = onMessageChanged,
                    label = { Text(text = "Invitation message (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                OutlinedButton(
                    onClick = onGenerateAiMessage,
                    enabled = !uiState.isGeneratingAiMessage && !uiState.isLoading && !uiState.isSending,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isGeneratingAiMessage) {
                            "Generating AI draft..."
                        } else {
                            "AI Help: Draft Invitation Message"
                        }
                    )
                }
            }

            uiState.successMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    AppInlineMessage(
                        message = message,
                        isError = false
                    )
                }
            }

            uiState.errorMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    AppInlineMessage(
                        message = message,
                        isError = true
                    )
                }
            }

            if (uiState.isLoading) {
                item { AppLoadingState(message = "Loading player candidates...") }
            }

            if (!uiState.isLoading && uiState.players.isEmpty()) {
                item {
                    InvitePlayersEmptyState(
                        title = "No players available",
                        message = "Create or seed player accounts, then return to invite them."
                    )
                }
            }

            if (!uiState.isLoading && uiState.players.isNotEmpty()) {
                item {
                    Text(
                        text = "Select Players",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.players, key = { player -> player.playerId }) { player ->
                    PlayerSelectableItem(
                        item = player,
                        onToggle = onTogglePlayer
                    )
                }
            }

            if (uiState.existingInvitations.isNotEmpty()) {
                item {
                    Text(
                        text = "Sent Invitations",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.existingInvitations, key = { invitation -> invitation.invitationId }) { invitation ->
                    InvitePlayersEmptyState(
                        title = invitation.playerName,
                        message = "${invitation.statusLabel} - Sent ${invitation.sentAtLabel}"
                    )
                }
            }

            item {
                Button(
                    onClick = onSendInvitations,
                    enabled = uiState.canSendInvites,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isSending) {
                            "Sending..."
                        } else {
                            "Send Invitations (${uiState.selectedPlayerIds.size})"
                        }
                    )
                }
            }

            item {
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Refresh")
                }
            }
        }
    }
}



