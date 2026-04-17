@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchCreateFormState
import com.dakti.app.presentation.matches.MatchReservationContextUi
import com.dakti.app.presentation.matches.MatchVenueOptionUi
import com.dakti.app.ui.components.SectionHeader

@Composable
fun CreateMatchScreen(
    formState: MatchCreateFormState,
    reservationContexts: List<MatchReservationContextUi>,
    venueOptions: List<MatchVenueOptionUi>,
    isSubmitting: Boolean,
    isCreateEnabled: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onReservationSelected: (String?) -> Unit,
    onVenueSelected: (String?) -> Unit,
    onSportTypeChanged: (String) -> Unit,
    onScheduledAtChanged: (String) -> Unit,
    onRequiredPlayersChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Create Match") },
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
                    title = "Match Setup",
                    subtitle = "Create from reservation context when available, or choose a venue directly."
                )
            }

            item {
                Text(
                    text = "Reservation Context",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = formState.selectedReservationId == null,
                            onClick = { onReservationSelected(null) },
                            label = { Text("No reservation") }
                        )
                    }
                    items(reservationContexts) { context ->
                        FilterChip(
                            selected = formState.selectedReservationId == context.reservationId,
                            onClick = { onReservationSelected(context.reservationId) },
                            label = { Text(context.displayLabel) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Venue",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(venueOptions) { venue ->
                        FilterChip(
                            selected = formState.selectedVenueId == venue.venueId,
                            onClick = { onVenueSelected(venue.venueId) },
                            label = { Text("${venue.venueName} (${venue.sportType})") }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = formState.sportType,
                    onValueChange = onSportTypeChanged,
                    label = { Text("Sport Type") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = formState.scheduledAtInput,
                    onValueChange = onScheduledAtChanged,
                    label = { Text("Scheduled At (yyyy-MM-dd HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = formState.requiredPlayersInput,
                    onValueChange = onRequiredPlayersChanged,
                    label = { Text("Required Players") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (successMessage != null) {
                item {
                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Button(
                    onClick = onCreateClick,
                    enabled = isCreateEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSubmitting) {
                            "Creating Match..."
                        } else {
                            "Create Match"
                        }
                    )
                }
            }
        }
    }
}
