package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.presentation.matches.MatchDetailsUi
import com.dakti.app.presentation.matches.MatchListItemUi
import com.dakti.app.presentation.matches.MatchReadinessUi
import com.dakti.app.ui.components.AppStateCard
import com.dakti.app.ui.components.daktiAccentCard
import com.dakti.app.ui.components.daktiCardBorder

@Composable
fun MatchStatusChip(
    statusLabel: String,
    status: MatchStatus? = null
) {
    val colors = when (status) {
        MatchStatus.CANCELLED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )

        MatchStatus.CONFIRMED,
        MatchStatus.FULL -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        MatchStatus.COMPLETED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
        modifier = Modifier
            .fillMaxWidth()
            .daktiAccentCard(shape = MaterialTheme.shapes.medium, elevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = daktiCardBorder()
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
            MatchStatusChip(
                statusLabel = match.statusLabel,
                status = match.status
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .daktiAccentCard(shape = MaterialTheme.shapes.medium, elevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = daktiCardBorder()
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
            MatchStatusChip(
                statusLabel = details.statusLabel,
                status = details.status
            )
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
        MatchReadinessStatus.READY -> MaterialTheme.colorScheme.tertiaryContainer
        MatchReadinessStatus.AT_RISK -> MaterialTheme.colorScheme.secondaryContainer
        MatchReadinessStatus.INSUFFICIENT_PLAYERS -> MaterialTheme.colorScheme.errorContainer
        MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> MaterialTheme.colorScheme.secondaryContainer
    }
    val border = if (readiness.status == MatchReadinessStatus.INSUFFICIENT_PLAYERS) {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.52f))
    } else {
        daktiCardBorder(strong = true)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .daktiAccentCard(shape = MaterialTheme.shapes.medium, elevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border
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
        modifier = Modifier
            .fillMaxWidth()
            .daktiAccentCard(shape = MaterialTheme.shapes.medium, elevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = daktiCardBorder()
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
    AppStateCard(
        title = title,
        message = message,
        actionLabel = actionLabel,
        onActionClick = onActionClick
    )
}
