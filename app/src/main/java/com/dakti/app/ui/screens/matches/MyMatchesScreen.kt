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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchListItemUi

@Composable
fun MyMatchesScreen(
    isLoading: Boolean,
    matches: List<MatchListItemUi>,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCreateMatch: () -> Unit,
    onMatchClick: (String) -> Unit
) {
    Scaffold(
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
                Text(
                    text = "Track your created matches and open details for invitation-ready actions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Button(onClick = onCreateMatch) {
                    Text(text = "Create Match")
                }
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator()
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
