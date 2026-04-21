package com.dakti.app.presentation.reservations

import com.dakti.app.domain.usecase.CreateReservationUseCase
import com.dakti.app.domain.usecase.GetMyReservationsUseCase
import com.dakti.app.domain.usecase.GetReservationDraftUseCase
import com.dakti.app.domain.usecase.SendReservationConfirmationNotificationUseCase
import com.dakti.app.testutil.FakeNotificationRepository
import com.dakti.app.testutil.FakeReservationRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val reservationRepository = FakeReservationRepository()
    private val notificationRepository = FakeNotificationRepository()

    private fun createViewModel(): ReservationViewModel =
        ReservationViewModel(
            getReservationDraftUseCase = GetReservationDraftUseCase(reservationRepository),
            createReservationUseCase = CreateReservationUseCase(reservationRepository),
            getMyReservationsUseCase = GetMyReservationsUseCase(reservationRepository),
            sendReservationConfirmationNotificationUseCase = SendReservationConfirmationNotificationUseCase(
                notificationRepository
            )
        )

    @Test
    fun loadReservationDraft_unavailableSlot_setsErrorMessage() = runTest {
        reservationRepository.draftResult = Resource.Success(
            TestData.reservationDraft(available = false)
        )
        val viewModel = createViewModel()

        viewModel.loadReservationDraft(venueId = "venue-1", timeSlotId = "slot-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.draft)
        assertEquals("Selected slot is no longer available.", state.confirmationErrorMessage)
    }

    @Test
    fun confirmReservation_whenAlreadySubmitting_doesNotTriggerDuplicateCreate() = runTest {
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft())
        reservationRepository.createResult = Resource.Loading
        val viewModel = createViewModel()
        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()

        viewModel.confirmReservation()
        viewModel.confirmReservation()
        advanceUntilIdle()

        assertEquals(1, reservationRepository.createCallCount)
        assertTrue(viewModel.uiState.value.isCreatingReservation)
    }

    @Test
    fun confirmReservation_success_updatesStateAndHistory() = runTest {
        val created = TestData.reservation(id = "res-42")
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft())
        reservationRepository.createResult = Resource.Success(created)
        reservationRepository.myReservationsResult = Resource.Success(listOf(created))
        notificationRepository.reservationNotificationResult = Resource.Success(Unit)
        val viewModel = createViewModel()

        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()
        viewModel.confirmReservation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("res-42", state.latestCreatedReservationId)
        assertTrue(state.reservationCreatedMessage?.contains("Reservation confirmed") == true)
        assertEquals(1, state.myReservations.size)
        assertFalse(state.draft?.isSlotAvailable ?: true)
    }
}
