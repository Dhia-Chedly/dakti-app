package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Reservation
import com.dakti.app.util.Resource

interface ReservationRepository {
    suspend fun getMyReservations(): Resource<List<Reservation>>
    suspend fun confirmReservation(venueId: String): Resource<Reservation>
}
