package com.dakti.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.home.HomeQuickActionType
import com.dakti.app.presentation.home.HomeUiState
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.theme.DaktiThemeTokens

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onBrowseVenues: () -> Unit,
    onCreateMatch: () -> Unit,
    onInvitePlayers: () -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenInvitations: () -> Unit,
    onAcceptInvitation: (String) -> Unit,
    onDeclineInvitation: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val dimensions = DaktiThemeTokens.dimensions

    DaktiHeroScaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensions.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(dimensions.sectionSpacing)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HomeHeaderRow(
                    greeting = uiState.header.greeting,
                    avatarUrl = uiState.header.avatarUrl,
                    onNotificationClick = {}
                )
            }

            item {
                HomeNextMatchCard(nextMatch = uiState.nextMatch)
            }

            item {
                HomeQuickActionGrid(
                    actions = uiState.quickActions,
                    onActionClick = { actionType ->
                        when (actionType) {
                            HomeQuickActionType.BOOK_VENUE -> onBrowseVenues()
                            HomeQuickActionType.CREATE_MATCH -> onCreateMatch()
                            HomeQuickActionType.INVITE_PLAYERS -> onInvitePlayers()
                            HomeQuickActionType.ASK_AI -> onOpenAssistant()
                        }
                    }
                )
            }

            item {
                HomeInsightBanner(
                    banner = uiState.insightBanner,
                    onCtaClick = {
                        if (uiState.nextMatch.hasMatch) {
                            onInvitePlayers()
                        } else {
                            onCreateMatch()
                        }
                    }
                )
            }

            item {
                HomeSectionHeader(
                    title = "Upcoming Invitations",
                    actionLabel = "See All",
                    onActionClick = onOpenInvitations
                )
            }

            if (uiState.isInvitationsLoading) {
                items(2) {
                    InvitationPlaceholderCard()
                }
            } else if (uiState.upcomingInvitations.isNotEmpty()) {
                items(uiState.upcomingInvitations, key = { invitation -> invitation.invitationId }) { invitation ->
                    HomeInvitationCard(
                        invitation = invitation,
                        onAccept = { onAcceptInvitation(invitation.invitationId) },
                        onDecline = { onDeclineInvitation(invitation.invitationId) }
                    )
                }
            } else {
                item {
                    EmptyStateCard(
                        message = uiState.invitationsMessage ?: "No pending invitations right now."
                    )
                }
            }

            item {
                HomeSectionHeader(
                    title = "Recommended Venues",
                    actionLabel = "See All",
                    onActionClick = onBrowseVenues
                )
            }

            item {
                if (uiState.isVenuesLoading) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(2) {
                            VenuePlaceholderCard()
                        }
                    }
                } else if (uiState.recommendedVenues.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.recommendedVenues, key = { venue -> venue.id }) { venue ->
                            HomeVenueCard(venue = venue)
                        }
                    }
                } else {
                    EmptyStateCard(message = "No venues available yet.")
                }
            }

            uiState.errorMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    ErrorStateCard(
                        message = message,
                        onRetry = onRefresh
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HomeHeaderRow(
    greeting: String,
    avatarUrl: String?,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeAvatar(
                avatarUrl = avatarUrl,
                modifier = Modifier
                    .width(46.dp)
                    .height(46.dp)
            )
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onNotificationClick) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

