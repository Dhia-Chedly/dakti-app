package com.dakti.app.ui.screens.matches

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dakti.app.presentation.matches.MatchCreateFormState
import com.dakti.app.presentation.matches.MatchVenueOptionUi
import com.dakti.app.ui.theme.DaktiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreateMatchScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sportDropdown_selectsOption_andInvokesCallback() {
        var selectedSport = ""

        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(),
                    venueOptions = sampleVenueOptions(),
                    isVenueOptionsLoading = false,
                    venueOptionsErrorMessage = null,
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = {},
                    onSportTypeChanged = { selectedSport = it },
                    onLocationChanged = {},
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("create_match_sport_dropdown_field").performClick()
        composeRule.onNodeWithTag("create_match_sport_dropdown_option_football").performClick()

        assertEquals("Football", selectedSport)
    }

    @Test
    fun locationDropdown_selectAnyLocation_andInvokesCallback() {
        var selectedLocation = "initial"

        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(sportType = "Football"),
                    venueOptions = sampleVenueOptions(),
                    isVenueOptionsLoading = false,
                    venueOptionsErrorMessage = null,
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = {},
                    onSportTypeChanged = {},
                    onLocationChanged = { selectedLocation = it },
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("create_match_location_dropdown_field").performClick()
        composeRule.onNodeWithTag("create_match_location_dropdown_option_empty").performClick()

        assertEquals("", selectedLocation)
    }

    @Test
    fun venueDropdown_selectsOption_andInvokesCallback() {
        var selectedVenue: String? = null

        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(
                        sportType = "Football",
                        selectedLocation = "Lagos"
                    ),
                    venueOptions = sampleVenueOptions(),
                    isVenueOptionsLoading = false,
                    venueOptionsErrorMessage = null,
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = { selectedVenue = it },
                    onSportTypeChanged = {},
                    onLocationChanged = {},
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("create_match_venue_dropdown_field").performClick()
        composeRule.onNodeWithTag("create_match_venue_dropdown_option_venue_1").performClick()

        assertEquals("venue-1", selectedVenue)
    }

    @Test
    fun selectedVenue_showsAvailabilityContext() {
        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(
                        sportType = "Football",
                        selectedLocation = "Lagos",
                        selectedVenueId = "venue-1"
                    ),
                    venueOptions = sampleVenueOptions(),
                    isVenueOptionsLoading = false,
                    venueOptionsErrorMessage = null,
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = {},
                    onSportTypeChanged = {},
                    onLocationChanged = {},
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Available slots (context): 3").assertIsDisplayed()
    }

    @Test
    fun noFilteredVenues_showsEmptyStateMessage() {
        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(
                        sportType = "Tennis",
                        selectedLocation = "Abuja"
                    ),
                    venueOptions = sampleVenueOptions(),
                    isVenueOptionsLoading = false,
                    venueOptionsErrorMessage = null,
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = {},
                    onSportTypeChanged = {},
                    onLocationChanged = {},
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = {},
                    onBack = {}
                )
            }
        }

        composeRule
            .onNodeWithText("No venues match the selected sport/location filters.")
            .assertIsDisplayed()
    }

    @Test
    fun dependencyError_showsRetryAndInvokesCallback() {
        var retried = false
        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(),
                    venueOptions = emptyList(),
                    isVenueOptionsLoading = false,
                    venueOptionsErrorMessage = "Venue options failed",
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = {},
                    onSportTypeChanged = {},
                    onLocationChanged = {},
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = { retried = true },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Could not load venue options").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun dependencyLoading_showsLoadingState() {
        composeRule.setContent {
            DaktiTheme {
                CreateMatchScreen(
                    formState = MatchCreateFormState(),
                    venueOptions = emptyList(),
                    isVenueOptionsLoading = true,
                    venueOptionsErrorMessage = null,
                    isSubmitting = false,
                    isCreateEnabled = false,
                    successMessage = null,
                    errorMessage = null,
                    onVenueSelected = {},
                    onSportTypeChanged = {},
                    onLocationChanged = {},
                    onScheduledAtChanged = {},
                    onRequiredPlayersChanged = {},
                    onDescriptionChanged = {},
                    onCreateClick = {},
                    onRetryDependencies = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Loading venue options...").assertIsDisplayed()
    }

    private fun sampleVenueOptions(): List<MatchVenueOptionUi> {
        return listOf(
            MatchVenueOptionUi(
                venueId = "venue-1",
                venueName = "Arena One",
                sportType = "Football",
                address = "1 Arena Road, Lagos",
                location = "Lagos",
                availableSlotsCount = 3
            ),
            MatchVenueOptionUi(
                venueId = "venue-2",
                venueName = "Arena Two",
                sportType = "Basketball",
                address = "2 Court Street, Abuja",
                location = "Abuja",
                availableSlotsCount = 1
            )
        )
    }
}
