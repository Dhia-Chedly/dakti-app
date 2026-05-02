@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.venues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.components.DaktiGlassTopBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.venues.VenueListItemUi
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.SectionHeader

@Composable
fun VenueListScreen(
    searchQuery: String,
    sportFilters: List<String>,
    selectedSportFilter: String,
    isLoading: Boolean,
    venues: List<VenueListItemUi>,
    errorMessage: String?,
    onSearchQueryChanged: (String) -> Unit,
    onSportFilterSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onVenueClick: (String) -> Unit
) {
    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(title = "Browse Venues")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "Find the right venue",
                    subtitle = "Search by name or location, filter by sport, and open details to continue booking."
                )
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    label = { Text(text = "Search venues or city") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        message = "No venues match your current search and filter.",
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

