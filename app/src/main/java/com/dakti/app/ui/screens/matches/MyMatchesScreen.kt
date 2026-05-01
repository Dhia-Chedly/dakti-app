@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import com.dakti.app.ui.components.DaktiHeroScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchListItemUi
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.SectionHeader

@Composable
fun MyMatchesScreen(
    isLoading: Boolean,
    matches: List<MatchListItemUi>,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCreateMatch: () -> Unit,
    onMatchClick: (String) -> Unit
) {
    DaktiHeroScaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "My Matches") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "My Matches",
                    subtitle = "Track participation progress and open details for organizer actions."
                )
            }

            item {
                Button(onClick = onCreateMatch) {
                    Text(text = "Create Match")
                }
            }

            if (isLoading) {
                item {
                    AppLoadingState(message = "Loading your matches...")
                }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    MatchEmptyState(
                        title = "Could not load matches",
                        message = errorMessage,
                        actionLabel = "Try Again",
                        onActionClick = onRefresh
                    )
                }
            }

            if (!isLoading && errorMessage == null && matches.isEmpty()) {
                item {
                    MatchEmptyState(
                        title = "No matches yet",
                        message = "Create a match from reservation context or selected venue.",
                        actionLabel = "Refresh",
                        onActionClick = onRefresh
                    )
                }
            }

            if (!isLoading && matches.isNotEmpty()) {
                items(matches, key = { match -> match.id }) { match ->
                    MatchCard(
                        match = match,
                        onOpenDetails = onMatchClick
                    )
                }
            }

            item {
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Refresh")
                }
            }
        }
    }
}

