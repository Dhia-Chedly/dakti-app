package com.dakti.app.presentation.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationDraft
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.usecase.CreateReservationUseCase
import com.dakti.app.domain.usecase.GetMyReservationsUseCase
import com.dakti.app.domain.usecase.GetReservationDraftUseCase
import com.dakti.app.domain.usecase.SendReservationConfirmationNotificationUseCase
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationDraftUi(
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val venueSportType: String,
    val timeSlotId: String,
    val timeSlotLabel: String,
    val priceLabel: String,
    val isSlotAvailable: Boolean
)

data class ReservationHistoryItemUi(
    val id: String,
    val venueName: String,
    val timeSlotLabel: String,
    val createdAtLabel: String,
    val status: ReservationStatus,
    val priceLabel: String
)

data class ReservationUiState(
    val isDraftLoading: Boolean = false,
    val draft: ReservationDraftUi? = null,
    val isCreatingReservation: Boolean = false,
    val reservationCreatedMessage: String? = null,
    val latestCreatedReservationId: String? = null,
    val confirmationErrorMessage: String? = null,
    val myReservations: List<ReservationHistoryItemUi> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyErrorMessage: String? = null
)

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val getReservationDraftUseCase: GetReservationDraftUseCase,
    private val createReservationUseCase: CreateReservationUseCase,
    private val getMyReservationsUseCase: GetMyReservationsUseCase,
    private val sendReservationConfirmationNotificationUseCase: SendReservationConfirmationNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    init {
        loadMyReservations()
    }

    fun loadReservationDraft(
        venueId: String,
        timeSlotId: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDraftLoading = true,
                    draft = null,
                    reservationCreatedMessage = null,
                    latestCreatedReservationId = null,
                    confirmationErrorMessage = null
                )
            }

            when (
                val result = getReservationDraftUseCase(
                    venueId = venueId,
                    timeSlotId = timeSlotId
                )
            ) {
                is Resource.Success -> {
                    val draftUi = result.data.toUi()
                    val slotAvailabilityError = if (!draftUi.isSlotAvailable) {
                        "Selected slot is no longer available."
                    } else {
                        null
                    }
                    _uiState.update {
                        it.copy(
                            isDraftLoading = false,
                            draft = draftUi,
                            confirmationErrorMessage = slotAvailabilityError
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDraftLoading = false,
                            draft = null,
                            confirmationErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isDraftLoading = true) }
                }
            }
        }
    }

    fun confirmReservation() {
        if (_uiState.value.isCreatingReservation) {
            return
        }

        val draft = _uiState.value.draft
        if (draft == null) {
            _uiState.update {
                it.copy(confirmationErrorMessage = "Reservation draft is not ready yet.")
            }
            return
        }

        if (!draft.isSlotAvailable) {
            _uiState.update {
                it.copy(confirmationErrorMessage = "Selected slot is no longer available.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingReservation = true,
                    reservationCreatedMessage = null,
                    latestCreatedReservationId = null,
                    confirmationErrorMessage = null
                )
            }

            when (
                val result = createReservationUseCase(
                    venueId = draft.venueId,
                    timeSlotId = draft.timeSlotId,
                    note = "Booked from mobile reservation flow"
                )
            ) {
                is Resource.Success -> {
                    val notificationResult = runCatching {
                        sendReservationConfirmationNotificationUseCase(result.data)
                    }.getOrElse { error ->
                        Resource.Error(
                            error.message ?: "Reservation confirmed, but notification could not be delivered."
                        )
                    }
                    val notificationHint = if (notificationResult is Resource.Error) {
                        " Notification permission may be disabled."
                    } else {
                        ""
                    }
                    _uiState.update {
                        it.copy(
                            isCreatingReservation = false,
                            draft = draft.copy(isSlotAvailable = false),
                            reservationCreatedMessage = "Reservation confirmed for ${result.data.venueName}.$notificationHint",
                            latestCreatedReservationId = result.data.id,
                            confirmationErrorMessage = null
                        )
                    }
                    loadMyReservations()
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingReservation = false,
                            reservationCreatedMessage = null,
                            latestCreatedReservationId = null,
                            confirmationErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isCreatingReservation = true) }
                }
            }
        }
    }

    fun loadMyReservations() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isHistoryLoading = true,
                    historyErrorMessage = null
                )
            }

            when (val result = getMyReservationsUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            myReservations = result.data.map { reservation -> reservation.toHistoryUi() }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            myReservations = emptyList(),
                            historyErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isHistoryLoading = true) }
                }
            }
        }
    }

    fun clearConfirmationFeedback() {
        _uiState.update {
            it.copy(
                reservationCreatedMessage = null,
                latestCreatedReservationId = null,
                confirmationErrorMessage = null
            )
        }
    }

    private fun ReservationDraft.toUi(): ReservationDraftUi =
        ReservationDraftUi(
            venueId = venueId,
            venueName = venueName,
            venueAddress = venueAddress,
            venueSportType = venueSportType,
            timeSlotId = timeSlotId,
            timeSlotLabel = timeSlotLabel,
            priceLabel = buildPriceLabel(totalPrice = totalPrice, currency = currency),
            isSlotAvailable = isSlotAvailable
        )

    private fun Reservation.toHistoryUi(): ReservationHistoryItemUi =
        ReservationHistoryItemUi(
            id = id,
            venueName = venueName,
            timeSlotLabel = timeSlot,
            createdAtLabel = createdAt.atZone(ZoneId.systemDefault()).format(historyDateFormatter),
            status = status,
            priceLabel = buildPriceLabel(totalPrice = totalPrice, currency = currency)
        )

    private fun buildPriceLabel(totalPrice: Double?, currency: String?): String {
        if (totalPrice == null || currency.isNullOrBlank()) {
            return "Not provided"
        }
        return "$currency ${totalPrice.toInt()}"
    }

    private companion object {
        private val historyDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
    }
}
