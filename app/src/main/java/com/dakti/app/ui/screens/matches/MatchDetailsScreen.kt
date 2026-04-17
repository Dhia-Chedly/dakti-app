@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchDetailsUi

@Composable
fun MatchDetailsScreen(
    isLoading: Boolean,
    details: MatchDetailsUi?,
    errorMessage: String?,
    onBack: () -> Unit,
    onInvitePlayers: () -> Unit,
    onSendReminder: () -> Unit,
    onAddToCalendar: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Match Details") },
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
            if (isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    MatchEmptyState(
                        title = "Match unavailable",
                        message = errorMessage,
                        actionLabel = "Back",
                        onActionClick = onBack
                    )
                }
            }

            if (!isLoading && details != null) {
                item {
                    MatchSummaryCard(details = details)
                }

                item {
                    OutlinedButton(
                        onClick = onInvitePlayers,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Invite Players")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onSendReminder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Reminder")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onAddToCalendar,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Add to Calendar")
                    }
                }
            }
        }
    }
}
