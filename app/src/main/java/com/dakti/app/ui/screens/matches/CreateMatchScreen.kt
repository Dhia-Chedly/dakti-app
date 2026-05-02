@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchCreateFormState
import com.dakti.app.presentation.matches.MatchReservationContextUi
import com.dakti.app.presentation.matches.MatchVenueOptionUi
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.AppStateCard
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.components.DaktiGlassTopBar
import com.dakti.app.ui.components.SectionHeader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private data class DropdownOption(
    val value: String,
    val label: String
)

private const val MIN_REQUIRED_PLAYERS = 2
private const val MAX_REQUIRED_PLAYERS = 30
private const val DEFAULT_REQUIRED_PLAYERS = 10

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
    onLocationChanged: (String) -> Unit,
    onScheduledAtChanged: (String) -> Unit,
    onRequiredPlayersChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {
    val sportOptions = remember(venueOptions) {
        venueOptions
            .map { venue -> venue.sportType }
            .distinct()
            .sorted()
            .map { sport -> DropdownOption(value = sport, label = sport) }
    }

    val locationOptions = remember(venueOptions, formState.sportType) {
        val filteredVenues = venueOptions.filter { venue ->
            formState.sportType.isBlank() || venue.sportType == formState.sportType
        }
        val locations = filteredVenues
            .map { venue -> venue.location }
            .distinct()
            .sorted()
        buildList {
            add(DropdownOption(value = "", label = "Any location"))
            locations.forEach { location ->
                add(DropdownOption(value = location, label = location))
            }
        }
    }

    val filteredVenueOptions = remember(venueOptions, formState.sportType, formState.selectedLocation) {
        venueOptions.filter { venue ->
            (formState.sportType.isBlank() || venue.sportType == formState.sportType) &&
                (formState.selectedLocation.isBlank() || venue.location == formState.selectedLocation)
        }
    }

    val venueDropdownOptions = remember(filteredVenueOptions) {
        filteredVenueOptions.map { venue ->
            DropdownOption(
                value = venue.venueId,
                label = "${venue.venueName} - ${venue.location}"
            )
        }
    }

    val selectedVenue = remember(formState.selectedVenueId, venueOptions) {
        venueOptions.firstOrNull { venue -> venue.venueId == formState.selectedVenueId }
    }

    val requiredPlayers = remember(formState.requiredPlayersInput) {
        (formState.requiredPlayersInput.toIntOrNull() ?: DEFAULT_REQUIRED_PLAYERS)
            .coerceIn(MIN_REQUIRED_PLAYERS, MAX_REQUIRED_PLAYERS)
    }

    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(
                title = "Create Match",
                onBack = onBack
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

            if (reservationContexts.isEmpty()) {
                item {
                    AppStateCard(
                        message = "No reservation context yet. You can still create a match by selecting a venue."
                    )
                }
            } else {
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
            }

            if (venueOptions.isEmpty()) {
                item {
                    AppStateCard(
                        title = "No venues available",
                        message = "Venue options are unavailable right now. Return to Venues and refresh data."
                    )
                }
            } else {
                item {
                    FormDropdownField(
                        label = "Sport Type",
                        selectedValue = formState.sportType,
                        options = sportOptions,
                        placeholder = "Select sport type",
                        onSelected = onSportTypeChanged
                    )
                }

                item {
                    FormDropdownField(
                        label = "Location",
                        selectedValue = formState.selectedLocation,
                        options = locationOptions,
                        placeholder = "Select location",
                        onSelected = onLocationChanged
                    )
                }

                item {
                    FormDropdownField(
                        label = "Venue",
                        selectedValue = formState.selectedVenueId.orEmpty(),
                        options = venueDropdownOptions,
                        placeholder = "Select venue",
                        onSelected = { venueId -> onVenueSelected(venueId.ifBlank { null }) }
                    )
                }

                if (filteredVenueOptions.isEmpty()) {
                    item {
                        AppStateCard(
                            message = "No venues match the selected sport/location filters."
                        )
                    }
                }

                if (selectedVenue != null) {
                    item {
                        Text(
                            text = "Address: ${selectedVenue.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                ScheduledAtPickerField(
                    value = formState.scheduledAtInput,
                    onValueChanged = onScheduledAtChanged
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Required Players: $requiredPlayers",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = requiredPlayers.toFloat(),
                        onValueChange = { value ->
                            onRequiredPlayersChanged(value.roundToInt().toString())
                        },
                        valueRange = MIN_REQUIRED_PLAYERS.toFloat()..MAX_REQUIRED_PLAYERS.toFloat(),
                        steps = MAX_REQUIRED_PLAYERS - MIN_REQUIRED_PLAYERS - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Choose between $MIN_REQUIRED_PLAYERS and $MAX_REQUIRED_PLAYERS players.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    AppInlineMessage(
                        message = errorMessage,
                        isError = true
                    )
                }
            }

            if (successMessage != null) {
                item {
                    AppInlineMessage(
                        message = successMessage,
                        isError = false
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

@Composable
private fun FormDropdownField(
    label: String,
    selectedValue: String,
    options: List<DropdownOption>,
    placeholder: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { option -> option.value == selectedValue }?.label.orEmpty()

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open $label options"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    expanded = true
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ScheduledAtPickerField(
    value: String,
    onValueChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val inputFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM yyyy h:mm a") }
    val displayValue = remember(value) {
        value
            .takeIf { dateValue -> dateValue.isNotBlank() }
            ?.let { dateValue ->
                runCatching {
                    LocalDateTime.parse(dateValue, inputFormatter).format(displayFormatter)
                }.getOrDefault(dateValue)
            }
            ?: "Select date and time"
    }

    fun openPicker() {
        val initial = runCatching {
            LocalDateTime.parse(value, inputFormatter)
        }.getOrElse {
            LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
        }

        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selectedDateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                        onValueChanged(selectedDateTime.format(inputFormatter))
                    },
                    initial.hour,
                    initial.minute,
                    false
                ).show()
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Scheduled At",
            style = MaterialTheme.typography.titleSmall
        )
        OutlinedButton(
            onClick = ::openPicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = displayValue)
        }
        Text(
            text = "Pick the match date and time from a calendar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
