package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.ReservationDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReservationRepositoryImpl @Inject constructor(
    private val reservationDao: ReservationDao,
    private val venueDao: VenueDao,
    private val userDao: UserDao
) : ReservationRepository {

    override suspend fun getMyReservations(): Resource<List<Reservation>> {
        ensureDemoOrganizerProfile()
        val reservations = reservationDao.getReservationsWithDetailsByOrganizer(DEMO_ORGANIZER_ID)
            .map { relation -> relation.toDomain() }
        return Resource.Success(reservations)
    }

    override suspend fun confirmReservation(venueId: String): Resource<Reservation> {
        ensureDemoOrganizerProfile()

        val venue = venueDao.getVenueById(venueId)
            ?: return Resource.Error("Venue not found")

        val timeSlot = venueDao.getFirstAvailableTimeSlot(venueId)
            ?: return Resource.Error("No available slot for this venue")

        val now = Instant.now()
        val reservation = Reservation(
            id = "res-${UUID.randomUUID()}",
            organizerId = DEMO_ORGANIZER_ID,
            venueId = venue.id,
            timeSlotId = timeSlot.id,
            venueName = venue.name,
            timeSlot = formatSlot(timeSlot.startTime, timeSlot.endTime),
            status = ReservationStatus.CONFIRMED,
            totalPrice = venue.pricePerHour,
            currency = venue.currency,
            note = "Created from placeholder flow",
            createdAt = now,
            updatedAt = now
        )

        reservationDao.upsertReservation(reservation.toEntity())
        venueDao.updateTimeSlot(timeSlot.copy(isAvailable = false))

        return Resource.Success(reservation)
    }

    override fun observeReservationsByOrganizer(organizerId: String): Flow<List<Reservation>> =
        reservationDao.observeReservationsWithDetailsByOrganizer(organizerId)
            .map { relations -> relations.map { relation -> relation.toDomain() } }

    override suspend fun createReservation(reservation: Reservation): Resource<Reservation> {
        reservationDao.upsertReservation(reservation.toEntity())
        return Resource.Success(reservation)
    }

    override suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Resource<Unit> {
        val reservation = reservationDao.getReservationById(reservationId)
            ?: return Resource.Error("Reservation not found")

        reservationDao.updateReservation(
            reservation.copy(
                status = status,
                updatedAt = Instant.now().toEpochMilli()
            )
        )

        return Resource.Success(Unit)
    }

    private suspend fun ensureDemoOrganizerProfile() {
        val now = Instant.now()
        userDao.upsertUser(
            User(
                id = DEMO_ORGANIZER_ID,
                displayName = "Dakti Organizer",
                email = "organizer@dakti.app",
                phoneNumber = null,
                avatarUrl = null,
                role = UserRole.BOTH,
                bio = "Host profile seed",
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )

        userDao.upsertOrganizer(
            Organizer(
                userId = DEMO_ORGANIZER_ID,
                rating = 4.8,
                totalHostedMatches = 0,
                organizationName = "Dakti Hosts",
                isVerified = true,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    private fun formatSlot(startMillis: Long, endMillis: Long): String {
        val zoneId = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val end = Instant.ofEpochMilli(endMillis).atZone(zoneId)
        return "${start.format(slotStartFormatter)} - ${end.format(slotEndFormatter)}"
    }

    companion object {
        private const val DEMO_ORGANIZER_ID: String = "organizer-demo"
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE HH:mm")
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
