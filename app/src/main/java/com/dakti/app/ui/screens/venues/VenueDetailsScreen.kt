@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.venues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.venues.VenueDetailsUi
import com.dakti.app.ui.components.AppLoadingState
import kotlinx.coroutines.launch

@Composable
fun VenueDetailsScreen(
    isLoading: Boolean,
    venueDetails: VenueDetailsUi?,
    selectedSlotId: String?,
    errorMessage: String?,
    onSlotSelected: (String) -> Unit,
    onContinueToReservation: (String) -> Unit,
    onOpenInMaps: () -> String?,
    onCallVenue: () -> String?,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Venue Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(text = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    AppLoadingState(message = "Loading venue details...")
                }
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    VenueErrorState(
                        message = errorMessage,
                        onRetry = onRetry
                    )
                }
            }

            venueDetails == null -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    VenueEmptyState(
                        message = "Venue details are not available right now.",
                        onRetry = onRetry
                    )
                }
            }

            else -> {
                val selectedSlot = venueDetails.timeSlots.firstOrNull { slot -> slot.id == selectedSlotId }
                val availableSlots = venueDetails.timeSlots.filter { slot -> slot.isAvailable }
                val canOpenMaps = venueDetails.address.isNotBlank() ||
                    (venueDetails.latitude != null && venueDetails.longitude != null)
                val canCallVenue = !venueDetails.contactPhone.isNullOrBlank()
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        VenueImagePlaceholder()
                    }

                    item {
                        Text(
                            text = venueDetails.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    item {
                        Text(
                            text = "${venueDetails.sportType} - ${venueDetails.locationLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Text(
                            text = venueDetails.address,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    item {
                        venueDetails.contactPhone?.let { phone ->
                            Text(
                                text = "Contact: $phone",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Text(
                            text = venueDetails.priceLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    item {
                        Text(
                            text = venueDetails.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (venueDetails.amenities.isNotEmpty()) {
                        item {
                            Text(
                                text = "Amenities: ${venueDetails.amenities.joinToString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val message = onOpenInMaps()
                                    message?.let { result ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(result)
                                        }
                                    }
                                },
                                enabled = canOpenMaps,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = null
                                )
                                Text(text = " Map")
                            }
                            OutlinedButton(
                                onClick = {
                                    val message = onCallVenue()
                                    message?.let { result ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(result)
                                        }
                                    }
                                },
                                enabled = canCallVenue,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Call,
                                    contentDescription = null
                                )
                                Text(text = " Call")
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Select Time Slot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    item {
                        Text(
                            text = "Unavailable slots are shown for visibility and cannot be selected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (venueDetails.timeSlots.isEmpty()) {
                        item {
                            VenueEmptyState(
                                message = "No time slots are published for this venue yet.",
                                onRetry = onRetry
                            )
                        }
                    } else {
                        items(venueDetails.timeSlots, key = { slot -> slot.id }) { slot ->
                            TimeSlotItem(
                                slot = slot,
                                isSelected = selectedSlotId == slot.id,
                                onClick = onSlotSelected
                            )
                        }
                    }

                    if (venueDetails.timeSlots.isNotEmpty() && availableSlots.isEmpty()) {
                        item {
                            Text(
                                text = "All listed slots are currently unavailable.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    selectedSlot?.let { slot ->
                        item {
                            Text(
                                text = "Selected slot: ${slot.timeLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                selectedSlotId?.let { slotId ->
                                    onContinueToReservation(slotId)
                                }
                            },
                            enabled = selectedSlotId != null && availableSlots.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Continue to Reservation")
                        }
                    }
                }
            }
        }
    }
}
