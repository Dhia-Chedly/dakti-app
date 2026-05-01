package com.dakti.app.presentation.invitations

import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.usecase.GenerateInvitationMessageUseCase
import com.dakti.app.domain.usecase.GetInviteCandidatesUseCase
import com.dakti.app.domain.usecase.GetMatchDetailsUseCase
import com.dakti.app.domain.usecase.GetMatchInvitationsUseCase
import com.dakti.app.domain.usecase.GetPlayerInvitationsUseCase
import com.dakti.app.domain.usecase.InvitePlayersUseCase
import com.dakti.app.domain.usecase.RespondToInvitationUseCase
import com.dakti.app.domain.usecase.ScheduleInvitationReminderUseCase
import com.dakti.app.testutil.FakeAssistantRepository
import com.dakti.app.testutil.FakeInvitationRepository
import com.dakti.app.testutil.FakeMatchRepository
import com.dakti.app.testutil.FakeNotificationRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InvitationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val invitationRepository = FakeInvitationRepository()
    private val matchRepository = FakeMatchRepository()
    private val notificationRepository = FakeNotificationRepository()
    private val assistantRepository = FakeAssistantRepository()

    private fun createViewModel(): InvitationViewModel =
        InvitationViewModel(
            getPlayerInvitationsUseCase = GetPlayerInvitationsUseCase(invitationRepository),
            respondToInvitationUseCase = RespondToInvitationUseCase(invitationRepository),
            getInviteCandidatesUseCase = GetInviteCandidatesUseCase(invitationRepository),
            invitePlayersUseCase = InvitePlayersUseCase(invitationRepository),
            getMatchInvitationsUseCase = GetMatchInvitationsUseCase(invitationRepository),
            getMatchDetailsUseCase = GetMatchDetailsUseCase(matchRepository),
            scheduleInvitationReminderUseCase = ScheduleInvitationReminderUseCase(notificationRepository),
            generateInvitationMessageUseCase = GenerateInvitationMessageUseCase(assistantRepository)
        )

    @Test
    fun respondToInvitation_success_setsActionMessage() = runTest {
        val viewModel = createViewModel()
        viewModel.loadPlayerInvitations()
        advanceUntilIdle()

        viewModel.respondToInvitation("inv-1", accept = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Invitation accepted.", state.actionMessage)
        assertFalse("inv-1" in state.respondingInvitationIds)
    }

    @Test
    fun sendInvitations_withoutSelectedPlayers_showsError() = runTest {
        val viewModel = createViewModel()
        viewModel.loadInvitePlayers("match-1")
        advanceUntilIdle()

        viewModel.sendInvitations()
        advanceUntilIdle()

        assertEquals("Select at least one player", viewModel.uiState.value.invitePlayers.errorMessage)
    }

    @Test
    fun sendInvitations_success_schedulesReminder() = runTest {
        invitationRepository.inviteResult = Resource.Success(1)
        val viewModel = createViewModel()
        viewModel.loadInvitePlayers("match-1")
        advanceUntilIdle()
        val playerId = viewModel.uiState.value.invitePlayers.players.first().playerId
        viewModel.togglePlayerSelection(playerId)

        viewModel.sendInvitations()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.actionMessage?.contains("Invitations sent successfully") == true)
        assertTrue(notificationRepository.scheduledInvitationReminderIds.contains("match-1"))
    }

    @Test
    fun sendInvitations_whenAlreadySending_ignoresDuplicateSubmit() = runTest {
        invitationRepository.inviteResult = Resource.Loading
        val viewModel = createViewModel()
        viewModel.loadInvitePlayers("match-1")
        advanceUntilIdle()
        val playerId = viewModel.uiState.value.invitePlayers.players.first().playerId
        viewModel.togglePlayerSelection(playerId)

        viewModel.sendInvitations()
        viewModel.sendInvitations()
        advanceUntilIdle()

        assertEquals(1, invitationRepository.invitedMatchIds.size)
        assertEquals("match-1", invitationRepository.invitedMatchIds.first())
    }

    @Test
    fun respondToInvitation_whenAlreadyResponding_ignoresDuplicateAction() = runTest {
        invitationRepository.respondResult = Resource.Loading
        val viewModel = createViewModel()
        viewModel.loadPlayerInvitations()
        advanceUntilIdle()

        viewModel.respondToInvitation("inv-1", accept = false)
        viewModel.respondToInvitation("inv-1", accept = true)
        advanceUntilIdle()

        assertTrue("inv-1" in viewModel.uiState.value.respondingInvitationIds)
    }
}
