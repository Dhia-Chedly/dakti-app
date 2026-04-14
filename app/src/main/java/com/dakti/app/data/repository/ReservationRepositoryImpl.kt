package com.dakti.app.data.repository

import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class ReservationRepositoryImpl @Inject constructor() : ReservationRepository {
    override suspend fun getMyReservations(): Resource<List<Reservation>> {
        return Resource.Success(
            listOf(
                Reservation(id = "res-1", venueName = "Central Football Arena", timeSlot = "Fri 20:00"),
                Reservation(id = "res-2", venueName = "City Padel Hub", timeSlot = "Sat 18:30")
            )
        )
    }

    override suspend fun confirmReservation(venueId: String): Resource<Reservation> {
        return Resource.Success(
            Reservation(
                id = "res-confirm-$venueId",
                venueName = "Placeholder Venue",
                timeSlot = "Upcoming slot"
            )
        )
    }
}
