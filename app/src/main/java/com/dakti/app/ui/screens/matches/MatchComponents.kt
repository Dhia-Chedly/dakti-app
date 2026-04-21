package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchDetailsUi
import com.dakti.app.presentation.matches.MatchListItemUi
import com.dakti.app.presentation.matches.MatchReadinessUi

@Composable
fun MatchStatusChip(statusLabel: String) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text = statusLabel) }
    )
}

@Composable
fun PlayersProgressIndicator(
    confirmedPlayers: Int,
    requiredPlayers: Int,
    modifier: Modifier = Modifier
) {
    val safeRequired = requiredPlayers.coerceAtLeast(1)
    val progress = (confirmedPlayers.toFloat() / safeRequired.toFloat()).coerceIn(0f, 1f)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "$confirmedPlayers / $requiredPlayers confirmed",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun MatchCard(
    match: MatchListItemUi,
    onOpenDetails: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = match.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${match.sportType} - ${match.venueName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = match.scheduledLabel,
                style = MaterialTheme.typography.bodySmall
            )
            MatchStatusChip(statusLabel = match.statusLabel)
            PlayersProgressIndicator(
                confirmedPlayers = match.confirmedPlayersCount,
                requiredPlayers = match.requiredPlayers
            )
            Text(
                text = "Invited: ${match.invitedPlayersCount} | Pending: ${match.pendingPlayersCount} | Declined: ${match.declinedPlayersCount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Remaining spots: ${match.remainingSpots}",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = { onOpenDetails(match.id) }) {
                Text(text = "View Details")
            }
        }
    }
}

@Composable
fun MatchSummaryCard(details: MatchDetailsUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = details.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${details.sportType} - ${details.venueName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = details.venueAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = details.scheduledLabel,
                style = MaterialTheme.typography.bodyMedium
            )
            MatchStatusChip(statusLabel = details.statusLabel)
            PlayersProgressIndicator(
                confirmedPlayers = details.confirmedPlayersCount,
                requiredPlayers = details.requiredPlayers
            )
            Text(
                text = "Invited players: ${details.invitedPlayersCount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Pending invitations: ${details.pendingPlayersCount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Declined invitations: ${details.declinedPlayersCount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Remaining spots: ${details.remainingSpots}",
                style = MaterialTheme.typography.bodySmall
            )
            details.reservationReference?.let { reference ->
                Text(
                    text = "Linked reservation: $reference",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            details.organizerName?.let { organizerName ->
                Text(
                    text = "Organizer: $organizerName",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            details.description?.takeIf { description -> description.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MatchReadinessCard(readiness: MatchReadinessUi) {
    val containerColor = when (readiness.status) {
        com.dakti.app.domain.model.MatchReadinessStatus.READY -> MaterialTheme.colorScheme.tertiaryContainer
        com.dakti.app.domain.model.MatchReadinessStatus.AT_RISK -> MaterialTheme.colorScheme.secondaryContainer
        com.dakti.app.domain.model.MatchReadinessStatus.INSUFFICIENT_PLAYERS -> MaterialTheme.colorScheme.errorContainer
        com.dakti.app.domain.model.MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Readiness: ${readiness.statusLabel}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = readiness.summary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = readiness.reason,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Confirmed ${readiness.confirmedPlayersCount}/${readiness.requiredPlayers}, " +
                    "Pending ${readiness.pendingPlayersCount}, Declined ${readiness.declinedPlayersCount}, " +
                    "Remaining ${readiness.remainingSpots}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun MatchMonitoringActionsCard(readiness: MatchReadinessUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Suggested Next Steps",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            readiness.suggestedActions.forEach { action ->
                Text(
                    text = "- ${action.title}",
                    style = MaterialTheme.typography.bodySmall
                )
                action.description?.takeIf { value -> value.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (readiness.reschedulingSuggestions.isNotEmpty()) {
                Text(
                    text = "Rescheduling options",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                readiness.reschedulingSuggestions.forEach { suggestion ->
                    Text(
                        text = "${suggestion.venueName} - ${suggestion.timeSlotLabel}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = suggestion.reason,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MatchEmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onActionClick) {
                Text(text = actionLabel)
            }
        }
    }
}
