package com.dakti.app.ui.screens.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.presentation.reservations.ReservationDraftUi
import com.dakti.app.presentation.reservations.ReservationHistoryItemUi
import com.dakti.app.ui.components.AppStateCard

@Composable
fun ReservationSummaryCard(draft: ReservationDraftUi) {
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
                text = draft.venueName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${draft.venueSportType} - ${draft.venueAddress}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Slot: ${draft.timeSlotLabel}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Price: ${draft.priceLabel}",
                style = MaterialTheme.typography.bodyMedium
            )
            ReservationStatusChip(
                status = if (draft.isSlotAvailable) {
                    ReservationStatus.PENDING
                } else {
                    ReservationStatus.CANCELLED
                }
            )
        }
    }
}

@Composable
fun ReservationCard(
    reservation: ReservationHistoryItemUi,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (onClick != null) {
                    base.clickable(onClick = onClick)
                } else {
                    base
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = reservation.venueName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = reservation.timeSlotLabel,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = reservation.createdAtLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReservationStatusChip(status = reservation.status)
                Text(
                    text = reservation.priceLabel,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ReservationStatusChip(status: ReservationStatus) {
    val label = when (status) {
        ReservationStatus.PENDING -> "Pending"
        ReservationStatus.CONFIRMED -> "Confirmed"
        ReservationStatus.CANCELLED -> "Cancelled"
        ReservationStatus.COMPLETED -> "Completed"
    }
    val colors = when (status) {
        ReservationStatus.PENDING -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        ReservationStatus.CONFIRMED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        ReservationStatus.CANCELLED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )

        ReservationStatus.COMPLETED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    AssistChip(
        onClick = {},
        enabled = false,
        colors = colors,
        label = { Text(text = label) }
    )
}

@Composable
fun ReservationEmptyState(
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    AppStateCard(
        message = message,
        actionLabel = actionLabel,
        onActionClick = onActionClick
    )
}

@Composable
fun ReservationSuccessContent(
    message: String,
    onMyReservationsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCreateMatchPlaceholder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            OutlinedButton(onClick = onMyReservationsClick) {
                Text(text = "View My Reservations")
            }
            OutlinedButton(onClick = onCreateMatchPlaceholder) {
                Text(text = "Create Match from Reservation")
            }
            OutlinedButton(onClick = onHomeClick) {
                Text(text = "Back to Home")
            }
        }
    }
}
