package com.dakti.app.ui.screens.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dakti.app.presentation.home.HomeHeaderUi
import com.dakti.app.presentation.home.HomeInsightBannerUi
import com.dakti.app.presentation.home.HomeInvitationPreviewUi
import com.dakti.app.presentation.home.HomeNextMatchUi
import com.dakti.app.presentation.home.HomeQuickActionType
import com.dakti.app.presentation.home.HomeQuickActionUi
import com.dakti.app.presentation.home.HomeRecommendedVenueUi
import com.dakti.app.presentation.home.HomeUiState
import com.dakti.app.ui.theme.DaktiTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeScreen_rendersMainSections() {
        composeRule.setContent {
            DaktiTheme {
                HomeScreen(
                    uiState = sampleState(),
                    onBrowseVenues = {},
                    onCreateMatch = {},
                    onInvitePlayers = {},
                    onOpenAssistant = {},
                    onOpenInvitations = {},
                    onAcceptInvitation = {},
                    onDeclineInvitation = {},
                    onRefresh = {}
                )
            }
        }

        composeRule.onNodeWithText("Upcoming Invitations").assertIsDisplayed()
        composeRule.onNodeWithText("Recommended Venues").assertIsDisplayed()
        composeRule.onNodeWithText("Book Venue").assertIsDisplayed()
        composeRule.onNodeWithText("Saturday 4:00 PM").assertIsDisplayed()
    }

    @Test
    fun homeScreen_bookVenueAction_callsCallback() {
        var called = false

        composeRule.setContent {
            DaktiTheme {
                HomeScreen(
                    uiState = sampleState(),
                    onBrowseVenues = { called = true },
                    onCreateMatch = {},
                    onInvitePlayers = {},
                    onOpenAssistant = {},
                    onOpenInvitations = {},
                    onAcceptInvitation = {},
                    onDeclineInvitation = {},
                    onRefresh = {}
                )
            }
        }

        composeRule.onNodeWithText("Book Venue").performClick()
        assertTrue(called)
    }

    @Test
    fun homeScreen_rendersMainSections_darkTheme() {
        composeRule.setContent {
            DaktiTheme(darkTheme = true) {
                HomeScreen(
                    uiState = sampleState(),
                    onBrowseVenues = {},
                    onCreateMatch = {},
                    onInvitePlayers = {},
                    onOpenAssistant = {},
                    onOpenInvitations = {},
                    onAcceptInvitation = {},
                    onDeclineInvitation = {},
                    onRefresh = {}
                )
            }
        }

        composeRule.onNodeWithText("Upcoming Invitations").assertIsDisplayed()
        composeRule.onNodeWithText("Recommended Venues").assertIsDisplayed()
    }

    private fun sampleState(): HomeUiState = HomeUiState(
        isLoading = false,
        header = HomeHeaderUi(greeting = "Hello, Champ!"),
        nextMatch = HomeNextMatchUi(
            hasMatch = true,
            dateTimeLabel = "Saturday 4:00 PM",
            venueLabel = "Main Stadium, Pitch 2",
            readinessLabel = "8/10 Ready",
            readinessProgress = 0.8f,
            remainingSpots = 2
        ),
        quickActions = listOf(
            HomeQuickActionUi(HomeQuickActionType.BOOK_VENUE, "Book Venue"),
            HomeQuickActionUi(HomeQuickActionType.CREATE_MATCH, "Create Match"),
            HomeQuickActionUi(HomeQuickActionType.INVITE_PLAYERS, "Invite Players"),
            HomeQuickActionUi(HomeQuickActionType.ASK_AI, "Ask AI", isPrimary = true)
        ),
        insightBanner = HomeInsightBannerUi(
            message = "You may need 2 more players for Saturday's match to complete the squad.",
            ctaLabel = "Find Players"
        ),
        upcomingInvitations = listOf(
            HomeInvitationPreviewUi(
                invitationId = "inv-1",
                matchId = "match-1",
                title = "Team Alpha vs Team Beta",
                scheduledLabel = "Today, 6:00 PM",
                canRespond = true,
                isResponding = false
            )
        ),
        isInvitationsLoading = false,
        recommendedVenues = listOf(
            HomeRecommendedVenueUi(
                id = "venue-1",
                name = "City Center Pitch",
                address = "12 Arena Road",
                sportType = "Football",
                imageUrl = null,
                distanceLabel = null,
                ratingLabel = null,
                priceLabel = "\$45/hr"
            )
        ),
        isVenuesLoading = false
    )
}
