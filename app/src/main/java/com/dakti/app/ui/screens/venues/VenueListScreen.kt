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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.venues.VenueListItemUi
import com.dakti.app.ui.components.SectionHeader

@Composable
fun VenueListScreen(
    isLoading: Boolean,
    venues: List<VenueListItemUi>,
    errorMessage: String?,
    onVenueClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredVenues = venues.filter { venue ->
        val matchesQuery =
            searchQuery.isBlank() ||
                venue.name.contains(searchQuery, ignoreCase = true) ||
                venue.city.contains(searchQuery, ignoreCase = true)

        val matchesFilter =
            selectedFilter == "All" || venue.sportType.equals(selectedFilter, ignoreCase = true)

        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Venues") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "Find a venue",
                    subtitle = "Browse available spaces and open details to continue booking flows."
                )
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by venue or city") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Football", "Padel", "Tennis").forEach { filter ->
                        AssistChip(
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
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

            if (!isLoading && filteredVenues.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No venues match your current search/filter.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(filteredVenues) { venue ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = venue.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${venue.sportType} • ${venue.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${venue.currency} ${venue.pricePerHour.toInt()} / hour",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onVenueClick(venue.id) }) {
                                Text(text = "View details")
                            }
                        }
                    }
                }
            }
        }
    }
}

