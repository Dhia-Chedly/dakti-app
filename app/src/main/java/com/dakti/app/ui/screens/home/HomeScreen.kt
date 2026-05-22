package com.dakti.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.home.HomeUiState
import com.dakti.app.ui.components.DaktiGlassTopBar
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
    val chrome = DaktiThemeTokens.chrome

    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(
                navigationIcon = {
                    HomeAvatar(
                        avatarUrl = uiState.header.avatarUrl,
                        modifier = Modifier.size(40.dp)
                    )
                },
                titleContent = {
                    androidx.compose.material3.Text(
                        text = uiState.header.greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = chrome.content
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = {}) {
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .background(
                                        color = chrome.selectedPill,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = chrome.selectedContent
                                )
                            }
                        }
                        if (uiState.header.notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Text(
                                    text = uiState.header.notificationCount.coerceAtMost(99).toString(),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensions.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(dimensions.sectionSpacing)
        ) {
            item {
                HomeNextMatchCard(
                    nextMatch = uiState.nextMatch,
                    onCreateMatch = onCreateMatch
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

