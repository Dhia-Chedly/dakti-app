package com.dakti.app.ui.screens.matches

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.presentation.matches.InviteDashboardCardUi
import com.dakti.app.presentation.matches.MatchDashboardCardUi
import com.dakti.app.presentation.matches.MatchesDashboardStatusTone
import com.dakti.app.presentation.matches.MatchesDashboardTab
import com.dakti.app.presentation.matches.MatchUiState
import com.dakti.app.ui.theme.DaktiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MatchesDashboardScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tabs_areDisplayed_andTabSelectionCallbackWorks() {
        var selectedTab: MatchesDashboardTab? = null

        composeRule.setContent {
            DaktiTheme {
                MatchesDashboardScreen(
                    uiState = sampleState(),
                    onCreateMatch = {},
                    onOpenMatchDetails = {},
                    onOpenAssistant = {},
                    onRefresh = {},
                    onTabSelected = { tab -> selectedTab = tab },
                    onSearchQueryChanged = {},
                    onToggleNeedsAttention = {},
                    onAcceptInvitation = {},
                    onDeclineInvitation = {}
                )
            }
        }

        composeRule.onNodeWithText("Upcoming").assertIsDisplayed()
        composeRule.onNodeWithText("Invites").assertIsDisplayed()
        composeRule.onNodeWithText("Past").assertIsDisplayed()

        composeRule.onNodeWithText("Invites").performClick()
        assertEquals(MatchesDashboardTab.INVITES, selectedTab)
    }

    @Test
    fun emptyUpcoming_showsCreateMatchCallToAction() {
        var createClicked = false
        composeRule.setContent {
            DaktiTheme {
                MatchesDashboardScreen(
                    uiState = sampleState().copy(
                        selectedTab = MatchesDashboardTab.UPCOMING,
                        upcomingMatches = emptyList()
                    ),
                    onCreateMatch = { createClicked = true },
                    onOpenMatchDetails = {},
                    onOpenAssistant = {},
                    onRefresh = {},
                    onTabSelected = {},
                    onSearchQueryChanged = {},
                    onToggleNeedsAttention = {},
                    onAcceptInvitation = {},
                    onDeclineInvitation = {}
                )
            }
        }

        composeRule.onNodeWithText("Create Match").assertIsDisplayed().performClick()
        assertTrue(createClicked)
    }

    private fun sampleState(): MatchUiState {
        return MatchUiState(
            isLoading = false,
            selectedTab = MatchesDashboardTab.UPCOMING,
            searchQuery = "",
            showNeedsAttentionOnly = false,
            unreadNotificationCount = 3,
            upcomingMatches = listOf(
                MatchDashboardCardUi(
                    id = "m1",
                    title = "Saturday Football Match",
                    sportType = "Football",
                    venueName = "Arena Plus",
                    scheduledLabel = "Sat, 17 May - 6:00 PM",
                    statusLabel = "Upcoming",
                    statusTone = MatchesDashboardStatusTone.WARNING,
                    requiredPlayers = 10,
                    confirmedPlayersCount = 8,
                    pendingPlayersCount = 2,
                    remainingSpots = 2,
                    needsAttention = true,
                    actionLabel = "Manage"
                )
            ),
            inviteItems = listOf(
                InviteDashboardCardUi(
                    invitationId = "inv-1",
                    matchId = "m1",
                    title = "Sunday Match",
                    sportType = "Football",
                    venueName = "Arena Plus",
                    scheduledLabel = "Sun, 18 May - 5:00 PM",
                    status = InvitationResponseStatus.PENDING,
                    statusLabel = "Pending",
                    canRespond = true,
                    isResponding = false
                )
            ),
            pastMatches = emptyList()
        )
    }
}
