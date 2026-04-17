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
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.launch

@Composable
fun VenueDetailsScreen(
    isLoading: Boolean,
    venueDetails: VenueDetailsUi?,
    errorMessage: String?,
    onContinueToReservation: () -> Unit,
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
                    CircularProgressIndicator()
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
                val availableSlots = venueDetails.timeSlots.filter { slot -> slot.isAvailable }
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
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Maps integration will be enabled in a later phase.")
                                    }
                                },
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
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Call integration will be enabled in a later phase.")
                                    }
                                },
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
                            text = "Available Time Slots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (availableSlots.isEmpty()) {
                        item {
                            VenueEmptyState(
                                message = "No available slots at the moment.",
                                onRetry = onRetry
                            )
                        }
                    } else {
                        items(availableSlots, key = { slot -> slot.id }) { slot ->
                            TimeSlotItem(slot = slot)
                        }
                    }

                    item {
                        Button(
                            onClick = onContinueToReservation,
                            enabled = availableSlots.isNotEmpty(),
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
