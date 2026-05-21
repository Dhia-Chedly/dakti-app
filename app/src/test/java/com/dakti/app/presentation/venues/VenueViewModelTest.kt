package com.dakti.app.presentation.venues

import com.dakti.app.domain.usecase.GetVenueDetailsUseCase
import com.dakti.app.domain.usecase.GetVenueSportTypesUseCase
import com.dakti.app.domain.usecase.SearchVenuesUseCase
import com.dakti.app.testutil.FakeVenueRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VenueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val venueRepository = FakeVenueRepository()

    private fun createViewModel(): VenueViewModel =
        VenueViewModel(
            searchVenuesUseCase = SearchVenuesUseCase(venueRepository),
            getVenueDetailsUseCase = GetVenueDetailsUseCase(venueRepository),
            getVenueSportTypesUseCase = GetVenueSportTypesUseCase(venueRepository)
        )

    @Test
    fun onSportFilterSelected_filtersVenueList() = runTest {
        venueRepository.venuesWithSlots = listOf(
            TestData.venueWithSlots(venueId = "venue-football", sportType = "Football"),
            TestData.venueWithSlots(venueId = "venue-basketball", sportType = "Basketball")
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSportFilterSelected("Basketball")
        advanceUntilIdle()

        val venues = viewModel.uiState.value.filteredVenues
        assertEquals(1, venues.size)
        assertEquals("Basketball", venues.first().sportType)
    }

    @Test
    fun selectTimeSlot_unavailableSlot_isIgnored() = runTest {
        venueRepository.venuesWithSlots = listOf(
            TestData.venueWithSlots(available = false)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadVenueDetails("venue-1")
        advanceUntilIdle()
        viewModel.selectTimeSlot("slot-1")

        assertNull(viewModel.uiState.value.selectedSlotId)
        assertTrue(viewModel.uiState.value.selectedVenueDetails?.timeSlots?.first()?.isAvailable == false)
    }

    @Test
    fun loadVenueDetails_withMissingBackendFields_formatsAsNotProvided() = runTest {
        val source = TestData.venueWithSlots()
        venueRepository.venuesWithSlots = listOf(
            source.copy(
                venue = source.venue.copy(
                    city = "",
                    state = null,
                    country = "",
                    pricePerHour = 0.0,
                    currency = ""
                )
            )
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadVenueDetails("venue-1")
        advanceUntilIdle()

        val details = viewModel.uiState.value.selectedVenueDetails
        assertEquals("Not provided", details?.locationLabel)
        assertEquals("Not provided", details?.priceLabel)
    }
}
