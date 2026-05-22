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
import org.junit.Assert.assertNull
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
    fun loadReservationDraft_availableSlot_populatesDraftWithoutError() = runTest {
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft(available = true))
        val viewModel = createViewModel()

        viewModel.loadReservationDraft(venueId = "venue-1", timeSlotId = "slot-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.draft)
        assertTrue(state.draft?.isSlotAvailable == true)
        assertNull(state.confirmationErrorMessage)
    }

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
    fun loadReservationDraft_error_setsErrorMessage() = runTest {
        reservationRepository.draftResult = Resource.Error("Draft fetch failed")
        val viewModel = createViewModel()

        viewModel.loadReservationDraft(venueId = "venue-1", timeSlotId = "slot-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.draft)
        assertEquals("Draft fetch failed", state.confirmationErrorMessage)
    }

    @Test
    fun loadReservationDraft_loading_keepsLoadingStateTrue() = runTest {
        reservationRepository.draftResult = Resource.Loading
        val viewModel = createViewModel()

        viewModel.loadReservationDraft(venueId = "venue-1", timeSlotId = "slot-1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDraftLoading)
    }

    @Test
    fun confirmReservation_withoutDraft_setsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.confirmReservation()

        assertEquals(
            "Reservation draft is not ready yet.",
            viewModel.uiState.value.confirmationErrorMessage
        )
        assertEquals(0, reservationRepository.createCallCount)
    }

    @Test
    fun confirmReservation_withUnavailableDraft_setsValidationError() = runTest {
        reservationRepository.draftResult = Resource.Success(
            TestData.reservationDraft(available = false)
        )
        val viewModel = createViewModel()
        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()

        viewModel.confirmReservation()

        assertEquals(
            "Selected slot is no longer available.",
            viewModel.uiState.value.confirmationErrorMessage
        )
        assertEquals(0, reservationRepository.createCallCount)
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
    fun confirmReservation_loading_keepsSubmittingStateTrue() = runTest {
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft())
        reservationRepository.createResult = Resource.Loading
        val viewModel = createViewModel()
        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()

        viewModel.confirmReservation()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCreatingReservation)
        assertEquals(1, reservationRepository.createCallCount)
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

    @Test
    fun confirmReservation_success_withNotificationFailure_showsNotificationHint() = runTest {
        val created = TestData.reservation(id = "res-77")
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft())
        reservationRepository.createResult = Resource.Success(created)
        reservationRepository.myReservationsResult = Resource.Success(listOf(created))
        notificationRepository.reservationNotificationResult = Resource.Error("Notifications unavailable")
        val viewModel = createViewModel()

        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()
        viewModel.confirmReservation()
        advanceUntilIdle()

        val message = viewModel.uiState.value.reservationCreatedMessage.orEmpty()
        assertTrue(message.contains("Reservation confirmed"))
        assertTrue(message.contains("Notification permission may be disabled."))
    }

    @Test
    fun confirmReservation_error_setsErrorMessage() = runTest {
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft())
        reservationRepository.createResult = Resource.Error("Reservation failed")
        val viewModel = createViewModel()
        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()

        viewModel.confirmReservation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingReservation)
        assertEquals("Reservation failed", state.confirmationErrorMessage)
        assertNull(state.latestCreatedReservationId)
    }

    @Test
    fun loadReservationDraft_withMissingPrice_formatsAsNotProvided() = runTest {
        reservationRepository.draftResult = Resource.Success(
            TestData.reservationDraft().copy(
                totalPrice = null,
                currency = null
            )
        )
        val viewModel = createViewModel()

        viewModel.loadReservationDraft(venueId = "venue-1", timeSlotId = "slot-1")
        advanceUntilIdle()

        assertEquals("Not provided", viewModel.uiState.value.draft?.priceLabel)
    }

    @Test
    fun loadMyReservations_success_populatesHistory() = runTest {
        val reservation = TestData.reservation(id = "res-99")
        reservationRepository.myReservationsResult = Resource.Success(listOf(reservation))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMyReservations()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.myReservations.size)
        assertNull(viewModel.uiState.value.historyErrorMessage)
    }

    @Test
    fun loadMyReservations_successWithEmptyList_clearsHistory() = runTest {
        reservationRepository.myReservationsResult = Resource.Success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMyReservations()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.myReservations.isEmpty())
        assertNull(viewModel.uiState.value.historyErrorMessage)
    }

    @Test
    fun loadMyReservations_error_setsHistoryError() = runTest {
        reservationRepository.myReservationsResult = Resource.Error("History failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMyReservations()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.myReservations.isEmpty())
        assertEquals("History failed", state.historyErrorMessage)
    }

    @Test
    fun loadMyReservations_loading_keepsHistoryLoadingTrue() = runTest {
        reservationRepository.myReservationsResult = Resource.Loading
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMyReservations()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isHistoryLoading)
    }

    @Test
    fun clearConfirmationFeedback_clearsFeedbackFields() = runTest {
        val created = TestData.reservation(id = "res-100")
        reservationRepository.draftResult = Resource.Success(TestData.reservationDraft())
        reservationRepository.createResult = Resource.Success(created)
        reservationRepository.myReservationsResult = Resource.Success(listOf(created))
        val viewModel = createViewModel()
        viewModel.loadReservationDraft("venue-1", "slot-1")
        advanceUntilIdle()
        viewModel.confirmReservation()
        advanceUntilIdle()

        viewModel.clearConfirmationFeedback()

        val state = viewModel.uiState.value
        assertNull(state.reservationCreatedMessage)
        assertNull(state.latestCreatedReservationId)
        assertNull(state.confirmationErrorMessage)
    }
}
