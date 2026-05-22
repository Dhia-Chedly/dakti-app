package com.dakti.app.presentation.matches

import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.usecase.CreateMatchUseCase
import com.dakti.app.domain.usecase.EvaluateMatchReadinessUseCase
import com.dakti.app.domain.usecase.GetMatchDetailsUseCase
import com.dakti.app.domain.usecase.GetMyMatchesUseCase
import com.dakti.app.domain.usecase.GetPlayerInvitationsUseCase
import com.dakti.app.domain.usecase.GetVenuesUseCase
import com.dakti.app.domain.usecase.ObserveNotificationsUseCase
import com.dakti.app.domain.usecase.RespondToInvitationUseCase
import com.dakti.app.domain.usecase.ScheduleMatchReminderUseCase
import com.dakti.app.testutil.FakeAuthRepository
import com.dakti.app.testutil.FakeAssistantRepository
import com.dakti.app.testutil.FakeInvitationRepository
import com.dakti.app.testutil.FakeMatchRepository
import com.dakti.app.testutil.FakeNotificationRepository
import com.dakti.app.testutil.FakeVenueRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.Resource
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private val invitationRepository = FakeInvitationRepository()
    private val authRepository = FakeAuthRepository()
    private val assistantRepository = FakeAssistantRepository()

    private fun createViewModel(): MatchViewModel =
        MatchViewModel(
            getMyMatchesUseCase = GetMyMatchesUseCase(matchRepository),
            getPlayerInvitationsUseCase = GetPlayerInvitationsUseCase(invitationRepository),
            respondToInvitationUseCase = RespondToInvitationUseCase(invitationRepository),
            observeNotificationsUseCase = ObserveNotificationsUseCase(notificationRepository),
            authRepository = authRepository,
            createMatchUseCase = CreateMatchUseCase(matchRepository),
            getMatchDetailsUseCase = GetMatchDetailsUseCase(matchRepository),
            getVenuesUseCase = GetVenuesUseCase(venueRepository),
            scheduleMatchReminderUseCase = ScheduleMatchReminderUseCase(notificationRepository),
            evaluateMatchReadinessUseCase = EvaluateMatchReadinessUseCase(assistantRepository)
        )

    @Test
    fun onCreateScreenOpened_resetsCreateForm() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onSportTypeChanged("Football")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")

        viewModel.onCreateScreenOpened()
        advanceUntilIdle()

        val form = viewModel.uiState.value.formState
        assertNull(form.selectedVenueId)
        assertEquals("", form.sportType)
        assertEquals("", form.scheduledAtInput)
    }

    @Test
    fun createMatch_withSelectedVenue_sendsNullReservationId() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        assertNull(matchRepository.lastCreatePayload?.reservationId)
    }

    @Test
    fun refresh_partitionsMatchesAndBuildsDashboardCounts() = runTest {
        val now = Instant.now()
        matchRepository.myMatchesResult = Resource.Success(
            listOf(
                buildMatch(
                    id = "future-1",
                    status = MatchStatus.ORGANIZING,
                    scheduledAt = now.plusSeconds(7200)
                ),
                buildMatch(
                    id = "past-1",
                    status = MatchStatus.COMPLETED,
                    scheduledAt = now.minusSeconds(7200)
                )
            )
        )
        invitationRepository.invitationsForPlayer = listOf(
            TestData.invitationWithContext(id = "inv-a", status = InvitationResponseStatus.PENDING),
            TestData.invitationWithContext(id = "inv-b", status = InvitationResponseStatus.ACCEPTED)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.upcomingCount)
        assertEquals(1, state.completedCount)
        assertEquals(1, state.invitesCount)
    }

    @Test
    fun onNeedsAttentionFilterToggled_filtersUpcomingCards() = runTest {
        val now = Instant.now()
        matchRepository.myMatchesResult = Resource.Success(
            listOf(
                buildMatch(
                    id = "needs-attention",
                    status = MatchStatus.ORGANIZING,
                    scheduledAt = now.plusSeconds(3600),
                    requiredPlayers = 10,
                    confirmed = 4,
                    pending = 2
                ),
                buildMatch(
                    id = "ready-match",
                    status = MatchStatus.CONFIRMED,
                    scheduledAt = now.plusSeconds(7200),
                    requiredPlayers = 4,
                    confirmed = 4,
                    pending = 0
                )
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.filteredUpcoming.size)

        viewModel.onNeedsAttentionFilterToggled()
        advanceUntilIdle()

        val filtered = viewModel.uiState.value.filteredUpcoming
        assertEquals(1, filtered.size)
        assertEquals("needs-attention", filtered.first().id)
    }

    @Test
    fun respondToInvitation_accept_callsUseCaseAndShowsActionMessage() = runTest {
        invitationRepository.invitationsForPlayer = listOf(
            TestData.invitationWithContext(id = "inv-respond", status = InvitationResponseStatus.PENDING)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.respondToInvitation("inv-respond", accept = true)
        advanceUntilIdle()

        assertTrue(
            invitationRepository.respondedInvitations.contains(
                "inv-respond" to InvitationResponseStatus.ACCEPTED
            )
        )
        val actionMessage = viewModel.uiState.value.actionMessage
        assertTrue(actionMessage == null || actionMessage == "Invitation accepted.")
    }

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
    fun createMatch_withNonNumericPlayers_setsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("abc")
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals("Required players must be a number", viewModel.uiState.value.createErrorMessage)
    }

    @Test
    fun createMatch_withPlayersBelowMinimum_setsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("1")
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals("Required players must be at least 2", viewModel.uiState.value.createErrorMessage)
    }

    @Test
    fun createMatch_withoutVenue_setsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSportTypeChanged("Football")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals("Select venue", viewModel.uiState.value.createErrorMessage)
    }

    @Test
    fun createMatch_withBlankSportType_setsValidationError() = runTest {
        venueRepository.venuesWithSlots = listOf(TestData.venueWithSlots(sportType = ""))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals("Sport type is required", viewModel.uiState.value.createErrorMessage)
    }

    @Test
    fun refreshMatchesModule_whenVenueOptionsLoadAfterMissingVenueError_clearsValidationError() = runTest {
        venueRepository.searchVenuesResult = Resource.Error("Venue options failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSportTypeChanged("Football")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()
        assertEquals("Select venue", viewModel.uiState.value.createErrorMessage)

        venueRepository.searchVenuesResult = null
        viewModel.refreshMatchesModule()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.createErrorMessage)
        assertTrue(viewModel.uiState.value.venueOptions.isNotEmpty())
    }

    @Test
    fun onVenueSelected_null_clearsSelectedVenue() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        assertNotNull(viewModel.uiState.value.formState.selectedVenueId)

        viewModel.onVenueSelected(null)
        assertNull(viewModel.uiState.value.formState.selectedVenueId)
    }

    @Test
    fun createMatch_error_updatesErrorMessageAndStopsSubmitting() = runTest {
        matchRepository.createMatchResult = Resource.Error("Create failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingMatch)
        assertEquals("Create failed", state.createErrorMessage)
        assertNull(state.latestCreatedMatchId)
    }

    @Test
    fun createMatch_loading_keepsSubmittingStateTrue() = runTest {
        matchRepository.createMatchResult = Resource.Loading
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCreatingMatch)
        assertEquals(1, matchRepository.createCallCount)
    }

    @Test
    fun createMatch_success_updatesFeedbackAndSchedulesReminder() = runTest {
        matchRepository.createMatchResult = Resource.Success(
            TestData.matchWithContext(id = "match-created")
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("match-created", state.latestCreatedMatchId)
        assertTrue(state.createSuccessMessage?.contains("Match created successfully") == true)
        assertTrue(notificationRepository.scheduledMatchReminderIds.contains("match-created"))
    }

    @Test
    fun createMatch_success_whenReminderSchedulingFails_showsHint() = runTest {
        matchRepository.createMatchResult = Resource.Success(
            TestData.matchWithContext(id = "match-reminder-fail")
        )
        notificationRepository.matchReminderScheduleResult = Resource.Error("No permission")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        advanceUntilIdle()

        val message = viewModel.uiState.value.createSuccessMessage.orEmpty()
        assertTrue(message.contains("Match created successfully"))
        assertTrue(message.contains("Match reminder could not be scheduled."))
    }

    @Test
    fun createMatch_whenSubmitting_ignoresDuplicateCalls() = runTest {
        matchRepository.createMatchResult = Resource.Loading
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onVenueSelected("venue-1")
        viewModel.onScheduledAtChanged("2026-05-21 18:00")
        viewModel.onRequiredPlayersChanged("10")
        viewModel.createMatch()
        viewModel.createMatch()
        advanceUntilIdle()

        assertEquals(1, matchRepository.createCallCount)
    }

    @Test
    fun init_whenVenueOptionsLoadFails_setsErrorMessage() = runTest {
        venueRepository.searchVenuesResult = Resource.Error("Venue options failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Venue options failed", viewModel.uiState.value.venueOptionsErrorMessage)
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

    private fun buildMatch(
        id: String,
        status: MatchStatus,
        scheduledAt: Instant,
        requiredPlayers: Int = 10,
        confirmed: Int = 6,
        pending: Int = 1
    ): MatchWithContext {
        val base = TestData.matchWithContext(
            id = id,
            status = status,
            requiredPlayers = requiredPlayers,
            confirmedPlayers = confirmed,
            pendingPlayers = pending,
            declinedPlayers = 0
        )
        return base.copy(
            match = Match(
                id = base.match.id,
                organizerId = base.match.organizerId,
                venueId = base.match.venueId,
                reservationId = base.match.reservationId,
                title = base.match.title,
                sportType = base.match.sportType,
                scheduledStartTime = scheduledAt,
                requiredPlayers = base.match.requiredPlayers,
                status = base.match.status,
                description = base.match.description,
                createdAt = base.match.createdAt,
                updatedAt = base.match.updatedAt
            )
        )
    }
}
