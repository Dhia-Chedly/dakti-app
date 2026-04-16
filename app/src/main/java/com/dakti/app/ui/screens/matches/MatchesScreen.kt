@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchListItemUi
import com.dakti.app.ui.components.DashboardActionCard
import com.dakti.app.ui.components.SectionHeader

@Composable
fun MatchesScreen(
    isLoading: Boolean,
    openMatchesCount: Int,
    matchesPreview: List<MatchListItemUi>,
    onCreateMatch: () -> Unit,
    onOpenMyMatches: () -> Unit,
    onOpenInvitations: () -> Unit,
    onOpenMatchDetails: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Matches") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "Match Hub",
                    subtitle = "Organize games, check your schedule, and track player invitations."
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Open or draft matches",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = openMatchesCount.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardActionCard(
                        title = "Create",
                        description = "Start a new match plan",
                        icon = Icons.Outlined.AddCircleOutline,
                        onClick = onCreateMatch,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardActionCard(
                        title = "My Matches",
                        description = "Open your full list",
                        icon = Icons.Outlined.SportsSoccer,
                        onClick = onOpenMyMatches,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                DashboardActionCard(
                    title = "Invitations",
                    description = "Manage invites and responses",
                    icon = Icons.Outlined.MailOutline,
                    onClick = onOpenInvitations,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionHeader(
                    title = "Upcoming Matches",
                    subtitle = "Preview of your latest match plans"
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            if (!isLoading && matchesPreview.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No matches yet. Use Create to set up your first game.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(matchesPreview.take(4)) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${item.status} • ${item.requiredPlayers} players",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { onOpenMatchDetails(item.id) }) {
                            Text(text = "Open match")
                        }
                    }
                }
            }
        }
    }
}

