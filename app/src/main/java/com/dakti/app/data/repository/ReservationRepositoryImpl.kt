package com.dakti.app.data.repository

import androidx.room.withTransaction
import com.dakti.app.data.local.dao.ReservationDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.local.database.AppDatabase
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationDraft
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
    private val userDao: UserDao,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val appDatabase: AppDatabase
) : ReservationRepository {

    override suspend fun getReservationDraft(
        venueId: String,
        timeSlotId: String
    ): Resource<ReservationDraft> {
        val organizerId = resolveOrganizerId()
        val venue = venueDao.getVenueById(venueId)
            ?: return Resource.Error("Venue not found")
        val slot = venueDao.getTimeSlotById(timeSlotId)
            ?: return Resource.Error("Selected slot not found")

        if (slot.venueId != venueId) {
            return Resource.Error("Selected slot does not belong to this venue")
        }

        return Resource.Success(
            ReservationDraft(
                organizerId = organizerId,
                venueId = venue.id,
                venueName = venue.name,
                venueAddress = venue.address,
                venueSportType = venue.sportType,
                timeSlotId = slot.id,
                timeSlotLabel = formatSlot(slot.startTime, slot.endTime),
                totalPrice = venue.pricePerHour,
                currency = venue.currency,
                isSlotAvailable = slot.isAvailable
            )
        )
    }

    override suspend fun getMyReservations(): Resource<List<Reservation>> {
        val organizerId = resolveOrganizerId()
        val reservations = reservationDao.getReservationsWithDetailsByOrganizer(organizerId)
            .map { relation -> relation.toDomain() }
        return Resource.Success(reservations)
    }

    override suspend fun getReservationById(reservationId: String): Resource<Reservation> {
        val reservation = reservationDao.getReservationWithDetails(reservationId)?.toDomain()
            ?: return Resource.Error("Reservation not found")
        return Resource.Success(reservation)
    }

    override suspend fun createReservation(
        venueId: String,
        timeSlotId: String,
        note: String?
    ): Resource<Reservation> {
        val organizerId = resolveOrganizerId()
        val venue = venueDao.getVenueById(venueId)
            ?: return Resource.Error("Venue not found")
        val slot = venueDao.getTimeSlotById(timeSlotId)
            ?: return Resource.Error("Selected slot not found")

        if (slot.venueId != venueId) {
            return Resource.Error("Selected slot does not belong to this venue")
        }

        if (!slot.isAvailable) {
            return Resource.Error("This slot has already been reserved")
        }

        val now = Instant.now()
        val reservation = Reservation(
            id = "res-${UUID.randomUUID()}",
            organizerId = organizerId,
            venueId = venue.id,
            timeSlotId = slot.id,
            venueName = venue.name,
            timeSlot = formatSlot(slot.startTime, slot.endTime),
            status = ReservationStatus.CONFIRMED,
            totalPrice = venue.pricePerHour,
            currency = venue.currency,
            note = note,
            createdAt = now,
            updatedAt = now
        )

        return try {
            appDatabase.withTransaction {
                val latestSlot = venueDao.getTimeSlotById(timeSlotId)
                    ?: throw IllegalStateException("Selected slot is no longer available")
                if (!latestSlot.isAvailable) {
                    throw IllegalStateException("This slot has already been reserved")
                }

                reservationDao.upsertReservation(reservation.toEntity())
                venueDao.updateTimeSlot(latestSlot.copy(isAvailable = false))
            }
            Resource.Success(reservation)
        } catch (exception: IllegalStateException) {
            Resource.Error(exception.message ?: "Could not reserve the selected slot")
        } catch (exception: Exception) {
            Resource.Error("Failed to create reservation")
        }
    }

    override fun observeReservationsByOrganizer(organizerId: String): Flow<List<Reservation>> =
        reservationDao.observeReservationsWithDetailsByOrganizer(organizerId)
            .map { relations -> relations.map { relation -> relation.toDomain() } }

    override suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Resource<Unit> {
        val reservation = reservationDao.getReservationById(reservationId)
            ?: return Resource.Error("Reservation not found")

        return try {
            appDatabase.withTransaction {
                reservationDao.updateReservation(
                    reservation.copy(
                        status = status,
                        updatedAt = Instant.now().toEpochMilli()
                    )
                )

                if (status == ReservationStatus.CANCELLED) {
                    val slot = venueDao.getTimeSlotById(reservation.timeSlotId)
                    if (slot != null) {
                        venueDao.updateTimeSlot(slot.copy(isAvailable = true))
                    }
                }
            }
            Resource.Success(Unit)
        } catch (exception: Exception) {
            Resource.Error("Failed to update reservation status")
        }
    }

    private suspend fun resolveOrganizerId(): String {
        val sessionUserId = sessionLocalDataSource.authenticatedUserId.value
        if (sessionUserId.isNullOrBlank()) {
            ensureDemoOrganizerProfile()
            return DEMO_ORGANIZER_ID
        }

        val user = userDao.getUserById(sessionUserId)
        if (user == null) {
            ensureDemoOrganizerProfile()
            return DEMO_ORGANIZER_ID
        }

        ensureOrganizerProfileForUser(user.id, user.displayName)
        return user.id
    }

    private suspend fun ensureOrganizerProfileForUser(
        userId: String,
        displayName: String
    ) {
        val withProfiles = userDao.getUserWithProfiles(userId)
        if (withProfiles?.organizer != null) {
            return
        }

        val now = Instant.now()
        userDao.upsertOrganizer(
            Organizer(
                userId = userId,
                rating = 0.0,
                totalHostedMatches = 0,
                organizationName = "$displayName Hosts",
                isVerified = false,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
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
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM HH:mm")
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
