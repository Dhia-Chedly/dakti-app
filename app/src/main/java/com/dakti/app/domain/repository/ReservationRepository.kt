package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun getMyReservations(): Resource<List<Reservation>>
    suspend fun confirmReservation(venueId: String): Resource<Reservation>

    fun observeReservationsByOrganizer(organizerId: String): Flow<List<Reservation>>
    suspend fun createReservation(reservation: Reservation): Resource<Reservation>
    suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Resource<Unit>
}
