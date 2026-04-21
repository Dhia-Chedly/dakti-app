package com.dakti.app.ui.screens.venues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.venues.VenueListItemUi
import com.dakti.app.presentation.venues.VenueTimeSlotUi
import com.dakti.app.ui.components.AppStateCard

@Composable
fun SportFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) }
    )
}

@Composable
fun VenueCard(
    venue: VenueListItemUi,
    onDetailsClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = venue.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${venue.sportType} - ${venue.locationLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = venue.priceLabel,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = venue.availabilityLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            venue.nextAvailableSlotLabel?.let { nextSlot ->
                Text(
                    text = "Next: $nextSlot",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDetailsClick(venue.id) }) {
                    Text(text = "View details")
                }
            }
        }
    }
}

@Composable
fun TimeSlotItem(
    slot: VenueTimeSlotUi,
    isSelected: Boolean,
    onClick: (String) -> Unit
) {
    val isAvailable = slot.isAvailable
    val containerColor = when {
        !isAvailable -> MaterialTheme.colorScheme.surfaceVariant
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable) {
                onClick(slot.id)
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = slot.timeLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (slot.isAvailable) "Available" else "Unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = if (slot.isAvailable) {
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            if (isSelected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            slot.capacityLabel?.let { capacity ->
                Text(
                    text = capacity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VenueEmptyState(
    message: String,
    onRetry: () -> Unit
) {
    AppStateCard(
        title = "No venues found",
        message = message,
        actionLabel = "Refresh",
        onActionClick = onRetry
    )
}

@Composable
fun VenueErrorState(
    message: String,
    onRetry: () -> Unit
) {
    AppStateCard(
        title = "Could not load venues",
        message = message,
        actionLabel = "Try again",
        onActionClick = onRetry,
        isError = true
    )
}

@Composable
fun VenueImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Venue Image Placeholder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
