package com.dakti.app.presentation.matches

import com.dakti.app.domain.usecase.CreateMatchUseCase
import com.dakti.app.domain.usecase.EvaluateMatchReadinessUseCase
import com.dakti.app.domain.usecase.GetMatchDetailsUseCase
import com.dakti.app.domain.usecase.GetMatchReservationContextsUseCase
import com.dakti.app.domain.usecase.GetMyMatchesUseCase
import com.dakti.app.domain.usecase.GetVenuesUseCase
import com.dakti.app.domain.usecase.ScheduleMatchReminderUseCase
import com.dakti.app.testutil.FakeAssistantRepository
import com.dakti.app.testutil.FakeMatchRepository
import com.dakti.app.testutil.FakeNotificationRepository
import com.dakti.app.testutil.FakeVenueRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val matchRepository = FakeMatchRepository()
    private val venueRepository = FakeVenueRepository()
    private val notificationRepository = FakeNotificationRepository()
    private val assistantRepository = FakeAssistantRepository()

    private fun createViewModel(): MatchViewModel =
        MatchViewModel(
            getMyMatchesUseCase = GetMyMatchesUseCase(matchRepository),
            createMatchUseCase = CreateMatchUseCase(matchRepository),
            getMatchDetailsUseCase = GetMatchDetailsUseCase(matchRepository),
            getMatchReservationContextsUseCase = GetMatchReservationContextsUseCase(matchRepository),
            getVenuesUseCase = GetVenuesUseCase(venueRepository),
            scheduleMatchReminderUseCase = ScheduleMatchReminderUseCase(notificationRepository),
            evaluateMatchReadinessUseCase = EvaluateMatchReadinessUseCase(assistantRepository)
        )

    @Test
    fun createMatch_withInvalidSchedule_setsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onSportTypeChanged("Football")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.onScheduledAtChanged("invalid")
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals("Use schedule format yyyy-MM-dd HH:mm", viewModel.uiState.value.createErrorMessage)
    }

    @Test
    fun onReservationContextSelected_null_clearsSelectedVenue() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCreateScreenOpened(prefilledReservationId = "res-1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.formState.selectedVenueId)

        viewModel.onReservationContextSelected(null)
        assertNull(viewModel.uiState.value.formState.selectedVenueId)
    }

    @Test
    fun createMatch_success_updatesFeedbackAndSchedulesReminder() = runTest {
        matchRepository.createMatchResult = Resource.Success(
            TestData.matchWithContext(id = "match-created")
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCreateScreenOpened(prefilledReservationId = "res-1")
        advanceUntilIdle()
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("match-created", state.latestCreatedMatchId)
        assertTrue(state.createSuccessMessage?.contains("Match created successfully") == true)
        assertTrue(notificationRepository.scheduledMatchReminderIds.contains("match-created"))
    }

    @Test
    fun createMatch_whenSubmitting_ignoresDuplicateCalls() = runTest {
        matchRepository.createMatchResult = Resource.Loading
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCreateScreenOpened(prefilledReservationId = "res-1")
        advanceUntilIdle()
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals(1, matchRepository.createCallCount)
    }

    @Test
    fun loadMatchDetails_success_loadsReadinessState() = runTest {
        assistantRepository.readinessByMatchId["match-1"] = TestData.readinessResult()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMatchDetails("match-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.selectedMatchDetails)
        assertNotNull(state.selectedMatchReadiness)
        assertEquals("At Risk", state.selectedMatchReadiness?.statusLabel)
    }

    @Test
    fun loadMatchDetails_withMissingBackendValues_formatsNotProvided() = runTest {
        matchRepository.detailsById["match-1"] = TestData.matchWithContext().copy(
            match = TestData.matchWithContext().match.copy(title = ""),
            venueName = "",
            venueAddress = ""
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMatchDetails("match-1")
        advanceUntilIdle()

        val details = viewModel.uiState.value.selectedMatchDetails
        assertEquals("Not provided", details?.title)
        assertEquals("Not provided", details?.venueName)
        assertEquals("Not provided", details?.venueAddress)
    }
}
