package com.dakti.app.presentation.home

import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.usecase.EvaluateMyMatchReadinessUseCase
import com.dakti.app.domain.usecase.GetMyMatchesUseCase
import com.dakti.app.domain.usecase.GetPlayerInvitationsUseCase
import com.dakti.app.domain.usecase.RespondToInvitationUseCase
import com.dakti.app.domain.usecase.SearchVenuesUseCase
import com.dakti.app.testutil.FakeAssistantRepository
import com.dakti.app.testutil.FakeAuthRepository
import com.dakti.app.testutil.FakeInvitationRepository
import com.dakti.app.testutil.FakeMatchRepository
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val matchRepository = FakeMatchRepository()
    private val assistantRepository = FakeAssistantRepository()
    private val invitationRepository = FakeInvitationRepository()
    private val venueRepository = FakeVenueRepository()

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            authRepository = authRepository,
            getMyMatchesUseCase = GetMyMatchesUseCase(matchRepository),
            evaluateMyMatchReadinessUseCase = EvaluateMyMatchReadinessUseCase(assistantRepository),
            getPlayerInvitationsUseCase = GetPlayerInvitationsUseCase(invitationRepository),
            respondToInvitationUseCase = RespondToInvitationUseCase(invitationRepository),
            searchVenuesUseCase = SearchVenuesUseCase(venueRepository)
        )

    @Test
    fun refreshHomeData_mapsLiveDataToSections() = runTest {
        authRepository.loginResult = Resource.Success(TestData.user(name = "Kelechi"))
        matchRepository.myMatchesResult = Resource.Success(listOf(futureMatchContext()))

        val viewModel = createViewModel()
        authRepository.login(email = "kelechi@dakti.app", password = "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Hello, Kelechi!", state.header.greeting)
        assertTrue(state.nextMatch.hasMatch)
        assertTrue(state.upcomingInvitations.isNotEmpty())
        assertTrue(state.recommendedVenues.isNotEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun refreshHomeData_withEmptySources_usesFallbacks() = runTest {
        matchRepository.myMatchesResult = Resource.Success(emptyList())
        invitationRepository.invitationsForPlayer = emptyList()
        venueRepository.venuesWithSlots = emptyList()
        assistantRepository.readinessByMatchId.clear()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.nextMatch.hasMatch)
        assertEquals("No pending invitations at the moment.", state.invitationsMessage)
        assertTrue(state.recommendedVenues.isEmpty())
    }

    @Test
    fun refreshHomeData_usesReadinessCountsForProgress() = runTest {
        matchRepository.myMatchesResult = Resource.Success(listOf(futureMatchContext()))
        assistantRepository.readinessByMatchId["match-1"] =
            TestData.readinessResult().copy(requiredPlayers = 12, confirmedPlayersCount = 9)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("9/12 Ready", state.nextMatch.readinessLabel)
        assertTrue(state.nextMatch.readinessProgress > 0.74f)
        assertTrue(state.nextMatch.readinessProgress < 0.76f)
    }

    @Test
    fun acceptInvitation_callsUseCaseAndShowsMessage() = runTest {
        invitationRepository.respondResult = Resource.Success(Unit)
        invitationRepository.invitationsForPlayer = listOf(TestData.invitationWithContext(id = "inv-home"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.acceptInvitation("inv-home")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(
            invitationRepository.respondedInvitations.contains(
                "inv-home" to InvitationResponseStatus.ACCEPTED
            )
        )
        assertEquals("Invitation accepted.", state.invitationsMessage)
    }

    private fun futureMatchContext() = TestData.matchWithContext().copy(
        match = TestData.matchWithContext().match.copy(
            scheduledStartTime = Instant.now().plusSeconds(2 * 60 * 60)
        )
    )
}
