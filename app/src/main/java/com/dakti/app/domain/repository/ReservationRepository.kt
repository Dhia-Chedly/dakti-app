package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationDraft
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun getReservationDraft(
        venueId: String,
        timeSlotId: String
    ): Resource<ReservationDraft>

    suspend fun getMyReservations(): Resource<List<Reservation>>
    suspend fun getReservationById(reservationId: String): Resource<Reservation>
    suspend fun createReservation(
        venueId: String,
        timeSlotId: String,
        note: String? = null
    ): Resource<Reservation>

    fun observeReservationsByOrganizer(organizerId: String): Flow<List<Reservation>>
    suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Resource<Unit>
}
