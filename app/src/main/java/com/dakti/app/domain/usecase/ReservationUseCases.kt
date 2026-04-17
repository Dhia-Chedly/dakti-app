package com.dakti.app.domain.usecase

import com.dakti.app.domain.repository.ReservationRepository
import javax.inject.Inject

class GetReservationDraftUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(
        venueId: String,
        timeSlotId: String
    ) = reservationRepository.getReservationDraft(venueId = venueId, timeSlotId = timeSlotId)
}

class CreateReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(
        venueId: String,
        timeSlotId: String,
        note: String? = null
    ) = reservationRepository.createReservation(
        venueId = venueId,
        timeSlotId = timeSlotId,
        note = note
    )
}

class GetMyReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke() = reservationRepository.getMyReservations()
}

class GetReservationDetailsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String) =
        reservationRepository.getReservationById(reservationId)
}
