@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.presentation.matches.InviteDashboardCardUi
import com.dakti.app.presentation.matches.MatchDashboardCardUi
import com.dakti.app.presentation.matches.MatchesDashboardStatusTone
import com.dakti.app.presentation.matches.MatchesDashboardTab
import com.dakti.app.presentation.matches.MatchUiState
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.DaktiGlassTopBar
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.theme.DaktiThemeTokens

@Composable
fun MatchesDashboardScreen(
    uiState: MatchUiState,
    onBack: (() -> Unit)? = null,
    onCreateMatch: () -> Unit,
    onOpenMatchDetails: (String) -> Unit,
    onOpenAssistant: () -> Unit,
    onRefresh: () -> Unit,
    onTabSelected: (MatchesDashboardTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleNeedsAttention: () -> Unit,
    onAcceptInvitation: (String) -> Unit,
    onDeclineInvitation: (String) -> Unit
) {
    val chrome = DaktiThemeTokens.chrome
    val semantic = DaktiThemeTokens.semantic

    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(
                titleContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "My Matches",
                            style = MaterialTheme.typography.headlineSmall,
                            color = chrome.content
                        )
                        Text(
                            text = "Track upcoming games, invites, and status.",
                            style = MaterialTheme.typography.bodySmall,
                            color = chrome.content.copy(alpha = 0.84f)
                        )
                    }
                },
                navigationIcon = onBack?.let { callback ->
                    {
                        IconButton(onClick = callback) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = chrome.content
                            )
                        }
                        if (uiState.unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(semantic.danger),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.unreadNotificationCount.coerceAtMost(99).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
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
            item {
                SegmentedMatchesTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        label = { Text(text = "Search matches") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.showNeedsAttentionOnly,
                        onClick = onToggleNeedsAttention,
                        label = { Text(text = "Needs Attention") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FilterAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            uiState.actionMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    AppInlineMessage(message = message, isError = false)
                }
            }

            uiState.errorMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    AppInlineMessage(message = message, isError = true)
                }
            }

            if (uiState.isLoading) {
                item {
                    AppLoadingState(message = "Loading match dashboard...")
                }
            } else {
                when (uiState.selectedTab) {
                    MatchesDashboardTab.UPCOMING -> {
                        if (uiState.filteredUpcoming.isEmpty()) {
                            item {
                                EmptyDashboardCard(
                                    title = "No upcoming matches",
                                    message = "Create a match to start inviting players.",
                                    onCreateMatch = onCreateMatch
                                )
                            }
                        } else {
                            items(uiState.filteredUpcoming, key = { item -> item.id }) { item ->
                                MatchDashboardCard(
                                    item = item,
                                    onOpenMatchDetails = onOpenMatchDetails
                                )
                            }
                        }
                    }
                    MatchesDashboardTab.INVITES -> {
                        if (uiState.filteredInvites.isEmpty()) {
                            item {
                                EmptyDashboardCard(
                                    title = "No invites",
                                    message = "When invitations arrive, they will appear here.",
                                    onCreateMatch = onCreateMatch,
                                    showCreateAction = false
                                )
                            }
                        } else {
                            items(uiState.filteredInvites, key = { item -> item.invitationId }) { invite ->
                                InvitationDashboardCard(
                                    item = invite,
                                    onAccept = onAcceptInvitation,
                                    onDecline = onDeclineInvitation
                                )
                            }
                        }
                    }
                    MatchesDashboardTab.PAST -> {
                        if (uiState.filteredPast.isEmpty()) {
                            item {
                                EmptyDashboardCard(
                                    title = "No past matches",
                                    message = "Completed and cancelled matches will show here.",
                                    onCreateMatch = onCreateMatch,
                                    showCreateAction = false
                                )
                            }
                        } else {
                            items(uiState.filteredPast, key = { item -> item.id }) { item ->
                                MatchDashboardCard(
                                    item = item,
                                    onOpenMatchDetails = onOpenMatchDetails
                                )
                            }
                        }
                    }
                }
            }

            item {
                AssistantBanner(onOpenAssistant = onOpenAssistant)
            }

            item {
                DashboardStatsStrip(
                    upcomingCount = uiState.upcomingCount,
                    invitesCount = uiState.invitesCount,
                    completedCount = uiState.completedCount
                )
            }

            item {
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Refresh")
                }
            }
        }
    }
}

@Composable
private fun SegmentedMatchesTabs(
    selectedTab: MatchesDashboardTab,
    onTabSelected: (MatchesDashboardTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MatchesDashboardTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Button(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        },
                        contentColor = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Text(text = tab.label())
                }
            }
        }
    }
}

@Composable
private fun MatchDashboardCard(
    item: MatchDashboardCardUi,
    onOpenMatchDetails: (String) -> Unit
) {
    val semantic = DaktiThemeTokens.semantic
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.sportType,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(
                    text = item.statusLabel,
                    tone = item.statusTone
                )
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${item.scheduledLabel} • ${item.venueName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "${item.confirmedPlayersCount}/${item.requiredPlayers} Confirmed",
                    color = semantic.success
                )
                MetricPill(
                    label = "${item.pendingPlayersCount} Pending",
                    color = semantic.warning
                )
                if (item.remainingSpots > 0) {
                    MetricPill(
                        label = "${item.remainingSpots} Need More",
                        color = semantic.info
                    )
                }
            }

            Button(
                onClick = { onOpenMatchDetails(item.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = item.actionLabel)
            }
        }
    }
}

@Composable
private fun InvitationDashboardCard(
    item: InviteDashboardCardUi,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.sportType,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(
                    text = item.statusLabel,
                    tone = when (item.status) {
                        InvitationResponseStatus.ACCEPTED -> MatchesDashboardStatusTone.POSITIVE
                        InvitationResponseStatus.DECLINED -> MatchesDashboardStatusTone.DANGER
                        InvitationResponseStatus.EXPIRED -> MatchesDashboardStatusTone.MUTED
                        InvitationResponseStatus.PENDING -> MatchesDashboardStatusTone.WARNING
                    }
                )
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${item.scheduledLabel} • ${item.venueName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.canRespond) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDecline(item.invitationId) },
                        enabled = !item.isResponding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Decline")
                    }
                    Button(
                        onClick = { onAccept(item.invitationId) },
                        enabled = !item.isResponding,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Accept")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    tone: MatchesDashboardStatusTone
) {
    val semantic = DaktiThemeTokens.semantic
    val container = when (tone) {
        MatchesDashboardStatusTone.POSITIVE -> semantic.success.copy(alpha = 0.18f)
        MatchesDashboardStatusTone.WARNING -> semantic.warning.copy(alpha = 0.2f)
        MatchesDashboardStatusTone.DANGER -> semantic.danger.copy(alpha = 0.18f)
        MatchesDashboardStatusTone.INFO -> semantic.info.copy(alpha = 0.16f)
        MatchesDashboardStatusTone.MUTED -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = when (tone) {
        MatchesDashboardStatusTone.POSITIVE -> semantic.success
        MatchesDashboardStatusTone.WARNING -> semantic.warning
        MatchesDashboardStatusTone.DANGER -> semantic.danger
        MatchesDashboardStatusTone.INFO -> semantic.info
        MatchesDashboardStatusTone.MUTED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

@Composable
private fun MetricPill(
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun AssistantBanner(
    onOpenAssistant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Let Dakti AI help organize your match",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Generate reminders and fill missing spots.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.88f)
                )
            }
            Button(onClick = onOpenAssistant) {
                Text(text = "Ask Dakti")
            }
        }
    }
}

@Composable
private fun DashboardStatsStrip(
    upcomingCount: Int,
    invitesCount: Int,
    completedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCell(label = "Upcoming", value = upcomingCount.toString())
            StatCell(label = "Invites", value = invitesCount.toString())
            StatCell(label = "Completed", value = completedCount.toString())
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyDashboardCard(
    title: String,
    message: String,
    onCreateMatch: () -> Unit,
    showCreateAction: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showCreateAction) {
                Button(
                    onClick = onCreateMatch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Create Match")
                }
            }
        }
    }
}

private fun MatchesDashboardTab.label(): String {
    return when (this) {
        MatchesDashboardTab.UPCOMING -> "Upcoming"
        MatchesDashboardTab.INVITES -> "Invites"
        MatchesDashboardTab.PAST -> "Past"
    }
}
