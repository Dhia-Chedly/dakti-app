package com.dakti.app.ui.screens.venues

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.dakti.app.presentation.venues.VenueDetailsUi
import com.dakti.app.presentation.venues.VenueTimeSlotUi
import com.dakti.app.ui.theme.DaktiTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VenueDetailsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mapAndCallButtons_enabled_whenPayloadDataAvailable_andInvokeCallbacks() {
        var mapClicked = false
        var callClicked = false

        composeRule.setContent {
            DaktiTheme {
                VenueDetailsScreen(
                    isLoading = false,
                    venueDetails = sampleVenueDetails(
                        address = "1 Arena Road, Lagos",
                        phone = "+2348011111111",
                        latitude = 6.45,
                        longitude = 3.39
                    ),
                    selectedSlotId = null,
                    errorMessage = null,
                    onSlotSelected = {},
                    onContinueToReservation = {},
                    onOpenInMaps = {
                        mapClicked = true
                        null
                    },
                    onCallVenue = {
                        callClicked = true
                        null
                    },
                    onRetry = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("venue_details_map_button").assertIsEnabled().performClick()
        composeRule.onNodeWithTag("venue_details_call_button").assertIsEnabled().performClick()

        assertTrue(mapClicked)
        assertTrue(callClicked)
    }

    @Test
    fun mapAndCallButtons_disabled_whenLocationAndPhoneUnavailable() {
        composeRule.setContent {
            DaktiTheme {
                VenueDetailsScreen(
                    isLoading = false,
                    venueDetails = sampleVenueDetails(
                        address = "",
                        phone = null,
                        latitude = null,
                        longitude = null
                    ),
                    selectedSlotId = null,
                    errorMessage = null,
                    onSlotSelected = {},
                    onContinueToReservation = {},
                    onOpenInMaps = { null },
                    onCallVenue = { null },
                    onRetry = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("venue_details_map_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("venue_details_call_button").assertIsNotEnabled()
    }

    private fun sampleVenueDetails(
        address: String,
        phone: String?,
        latitude: Double?,
        longitude: Double?
    ): VenueDetailsUi {
        return VenueDetailsUi(
            id = "venue-1",
            name = "Arena One",
            sportType = "Football",
            locationLabel = "Lagos, Nigeria",
            address = address,
            contactPhone = phone,
            latitude = latitude,
            longitude = longitude,
            description = "Main pitch",
            imageUrl = null,
            priceLabel = "NGN 12000 / hour",
            amenities = listOf("Parking"),
            timeSlots = listOf(
                VenueTimeSlotUi(
                    id = "slot-1",
                    timeLabel = "Fri, 21 May 18:00 - 19:00",
                    isAvailable = true,
                    capacityLabel = "Capacity 14"
                )
            )
        )
    }
}
