@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.venues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.venues.VenueAdvancedFilter
import com.dakti.app.presentation.venues.VenueListItemUi
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.DaktiGlassTopBar
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.components.SectionHeader

@Composable
fun VenueListScreen(
    searchQuery: String,
    sportFilters: List<String>,
    selectedSportFilter: String,
    selectedAdvancedFilter: VenueAdvancedFilter,
    locationPermissionGranted: Boolean,
    isResolvingLocation: Boolean,
    locationMessage: String?,
    isLoading: Boolean,
    venues: List<VenueListItemUi>,
    errorMessage: String?,
    onSearchQueryChanged: (String) -> Unit,
    onSportFilterSelected: (String) -> Unit,
    onAdvancedFilterSelected: (VenueAdvancedFilter) -> Unit,
    onRequestLocationPermission: () -> Unit,
    onRefreshLocation: () -> Unit,
    onRetry: () -> Unit,
    onVenueClick: (String) -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(title = "Venues")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "Find and book the best sports venues",
                    subtitle = "Search, filter by sport, and sort by availability or nearest venues."
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        label = { Text(text = "Search venues, sports, or locations") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick = { showFilterMenu = true }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = "Filter venues"
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        VenueAdvancedFilter.entries.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(text = filter.label()) },
                                onClick = {
                                    showFilterMenu = false
                                    onAdvancedFilterSelected(filter)
                                }
                            )
                        }
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sportFilters) { filter ->
                        SportFilterChip(
                            label = filter,
                            selected = selectedSportFilter == filter,
                            onClick = { onSportFilterSelected(filter) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            if (locationPermissionGranted) {
                                onRefreshLocation()
                            } else {
                                onRequestLocationPermission()
                            }
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = "Refresh location distance"
                        )
                    }
                    Text(
                        text = if (isResolvingLocation) {
                            "Resolving your location..."
                        } else if (locationPermissionGranted) {
                            "Location is on. Distances are shown when available."
                        } else {
                            "Location is off. Turn it on to see distances."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    )
                }
            }

            locationMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    AppInlineMessage(
                        message = message,
                        isError = false
                    )
                }
            }

            if (isLoading) {
                item {
                    AppLoadingState(message = "Loading venues...")
                }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    VenueErrorState(
                        message = errorMessage,
                        onRetry = onRetry
                    )
                }
            }

            if (!isLoading && errorMessage == null && venues.isEmpty()) {
                item {
                    VenueEmptyState(
                        message = "No venues match your current search and filters.",
                        onRetry = onRetry
                    )
                }
            }

            if (!isLoading && venues.isNotEmpty()) {
                items(venues, key = { venue -> venue.id }) { venue ->
                    VenueCard(
                        venue = venue,
                        onDetailsClick = onVenueClick
                    )
                }
            }
        }
    }
}

private fun VenueAdvancedFilter.label(): String {
    return when (this) {
        VenueAdvancedFilter.ALL -> "All Venues"
        VenueAdvancedFilter.AVAILABLE_ONLY -> "Available Now"
        VenueAdvancedFilter.NEARBY_FIRST -> "Nearby First"
    }
}
