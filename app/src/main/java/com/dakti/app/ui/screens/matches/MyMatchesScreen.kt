@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
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
    matches: List<MatchListItemUi>,
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
                    text = "Track planned and active matches in one place.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Button(onClick = onCreateMatch) {
                    Text(text = "Create Match")
                }
            }

            if (matches.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No matches yet. Create one to get started.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(matches) { match ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = match.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Status: ${match.status}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Required players: ${match.requiredPlayers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { onMatchClick(match.id) }) {
                            Text(text = "View details")
                        }
                    }
                }
            }
        }
    }
}

