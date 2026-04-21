@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.home.HomeUiState
import com.dakti.app.ui.components.DashboardActionCard
import com.dakti.app.ui.components.SectionHeader

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onBrowseVenues: () -> Unit,
    onCreateMatch: () -> Unit,
    onMyReservations: () -> Unit,
    onMyMatches: () -> Unit,
    onOpenAssistant: () -> Unit,
    onRefreshMonitoring: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Home") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = uiState.greetingTitle,
                subtitle = uiState.summaryText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "Browse Venues",
                    description = "Find available spaces and check slots.",
                    icon = Icons.Outlined.Stadium,
                    onClick = onBrowseVenues,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionCard(
                    title = "Create Match",
                    description = "Start planning your next game.",
                    icon = Icons.Outlined.SportsSoccer,
                    onClick = onCreateMatch,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "My Reservations",
                    description = "Track pending and confirmed bookings.",
                    icon = Icons.Outlined.EventAvailable,
                    onClick = onMyReservations,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionCard(
                    title = "My Matches",
                    description = "Review your scheduled match plans.",
                    icon = Icons.AutoMirrored.Outlined.FactCheck,
                    onClick = onMyMatches,
                    modifier = Modifier.weight(1f)
                )
            }

            DashboardActionCard(
                title = "Open Assistant",
                description = "Get help with match formats and planning prompts.",
                icon = Icons.Outlined.AutoAwesome,
                onClick = onOpenAssistant,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.readinessAlertCount > 0) {
                SectionHeader(
                    title = "Matches Need Attention",
                    subtitle = "${uiState.readinessAlertCount} active alert(s) from readiness monitoring"
                )
                uiState.readinessHighlights.forEach { item ->
                    InfoCard(text = item)
                }
                TextButton(
                    onClick = onRefreshMonitoring,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Refresh Monitoring Alerts")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader(
                title = "Upcoming Actions",
                subtitle = "Shortlist of things to tackle next"
            )
            uiState.upcomingActions.forEach { item ->
                InfoCard(text = item)
            }

            SectionHeader(
                title = "Recent Activity",
                subtitle = "Placeholder activity stream for now"
            )
            uiState.recentActivity.forEach { item ->
                InfoCard(text = item)
            }
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}

