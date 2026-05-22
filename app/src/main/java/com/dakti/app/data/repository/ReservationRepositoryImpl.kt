package com.dakti.app.data.repository

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import com.dakti.app.data.remote.supabase.model.ReservationRowDto
import com.dakti.app.data.remote.supabase.model.TimeSlotRowDto
import com.dakti.app.data.remote.supabase.model.VenueRowDto
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationDraft
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.round
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class ReservationRepositoryImpl @Inject constructor(
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource
) : ReservationRepository {

    private val reservationsCache = MutableStateFlow<Map<String, List<Reservation>>>(emptyMap())

    override suspend fun getReservationDraft(
        venueId: String,
        timeSlotId: String
    ): Resource<ReservationDraft> {
        return runCatching {
            val organizerId = resolveOrganizerId() ?: return@runCatching Resource.Error("No authenticated user")
            val venue = supabaseRemoteDataSource.getVenueById(venueId)
                ?: return@runCatching Resource.Error("Venue not found")
            val slot = supabaseRemoteDataSource.getTimeSlotById(timeSlotId)
                ?: return@runCatching Resource.Error("Selected slot not found")

            if (slot.venueId != venueId) {
                return@runCatching Resource.Error("Selected slot does not belong to this venue")
            }
            val estimatedTotalPrice = estimateTotalPrice(venue = venue, slot = slot)

            Resource.Success(
                ReservationDraft(
                    organizerId = organizerId,
                    venueId = venue.id,
                    venueName = venue.name,
                    venueAddress = venue.address,
                    venueSportType = venue.sportType,
                    timeSlotId = slot.id,
                    timeSlotLabel = slot.toLabel(),
                    totalPrice = estimatedTotalPrice,
                    currency = venue.currency,
                    isSlotAvailable = slot.isAvailable
                )
            )
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not prepare reservation")
        }
    }

    override suspend fun getMyReservations(): Resource<List<Reservation>> {
        val organizerId = resolveOrganizerId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val rows = supabaseRemoteDataSource.getReservationsByOrganizer(organizerId)
            val reservations = hydrateReservations(rows)
            reservationsCache.value = reservationsCache.value + (organizerId to reservations)
            Resource.Success(reservations)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch reservations")
        }
    }

    override suspend fun getReservationById(reservationId: String): Resource<Reservation> {
        return runCatching {
            val row = supabaseRemoteDataSource.getReservationById(reservationId)
                ?: return@runCatching Resource.Error("Reservation not found")
            val reservation = hydrateReservations(listOf(row)).firstOrNull()
                ?: return@runCatching Resource.Error("Reservation details unavailable")
            Resource.Success(reservation)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch reservation")
        }
    }

    override suspend fun createReservation(
        venueId: String,
        timeSlotId: String,
        note: String?
    ): Resource<Reservation> {
        val organizerId = resolveOrganizerId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val venue = supabaseRemoteDataSource.getVenueById(venueId)
                ?: return@runCatching Resource.Error("Venue not found")
            val slot = supabaseRemoteDataSource.getTimeSlotById(timeSlotId)
                ?: return@runCatching Resource.Error("Selected slot not found")

            if (slot.venueId != venueId) {
                return@runCatching Resource.Error("Selected slot does not belong to this venue")
            }
            if (!slot.isAvailable) {
                return@runCatching Resource.Error("This slot has already been reserved")
            }
            val totalPrice = estimateTotalPrice(venue = venue, slot = slot)
            val reservationCurrency = venue.currency?.ifBlank { null }

            val created = supabaseRemoteDataSource.createReservation(
                payload = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "organizer_id" to organizerId,
                    "venue_id" to venueId,
                    "time_slot_id" to timeSlotId,
                    "status" to "confirmed",
                    "notes" to note,
                    "total_price" to totalPrice,
                    "currency" to reservationCurrency
                )
            ) ?: return@runCatching Resource.Error("Could not create reservation")

            supabaseRemoteDataSource.updateTimeSlot(
                slotId = timeSlotId,
                payload = mapOf("is_available" to false)
            )

            val reservation = Reservation(
                id = created.id,
                organizerId = created.organizerId,
                venueId = created.venueId,
                timeSlotId = created.timeSlotId,
                venueName = venue.name,
                timeSlot = slot.toLabel(),
                status = created.status.toReservationStatus(),
                totalPrice = created.totalPrice ?: totalPrice,
                currency = created.currency ?: reservationCurrency,
                note = created.notes,
                createdAt = created.createdAt.toInstantOrNow(),
                updatedAt = (created.updatedAt ?: created.createdAt).toInstantOrNow()
            )

            val current = reservationsCache.value[organizerId].orEmpty()
            reservationsCache.value = reservationsCache.value + (organizerId to (listOf(reservation) + current))
            Resource.Success(reservation)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not create reservation")
        }
    }

    override fun observeReservationsByOrganizer(organizerId: String): Flow<List<Reservation>> =
        reservationsCache
            .asStateFlow()
            .map { cache -> cache[organizerId].orEmpty() }

    override suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Resource<Unit> {
        return runCatching {
            val existing = supabaseRemoteDataSource.getReservationById(reservationId)
                ?: return@runCatching Resource.Error("Reservation not found")

            supabaseRemoteDataSource.updateReservation(
                reservationId = reservationId,
                payload = mapOf(
                    "status" to status.toRemoteStatus(),
                    "updated_at" to Instant.now().toString()
                )
            )

            if (status == ReservationStatus.CANCELLED) {
                supabaseRemoteDataSource.updateTimeSlot(
                    slotId = existing.timeSlotId,
                    payload = mapOf("is_available" to true)
                )
            }
            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not update reservation")
        }
    }

    private suspend fun hydrateReservations(rows: List<ReservationRowDto>): List<Reservation> {
        if (rows.isEmpty()) {
            return emptyList()
        }

        val venueIds = rows.map { it.venueId }.distinct()
        val slotIds = rows.map { it.timeSlotId }.distinct()

        val venues = venueIds.associateWith { id -> supabaseRemoteDataSource.getVenueById(id) }
        val slots = slotIds.associateWith { id -> supabaseRemoteDataSource.getTimeSlotById(id) }

        return rows.map { row ->
            val venue = venues[row.venueId]
            val slot = slots[row.timeSlotId]
            Reservation(
                id = row.id,
                organizerId = row.organizerId,
                venueId = row.venueId,
                timeSlotId = row.timeSlotId,
                venueName = venue?.name.orEmpty(),
                timeSlot = slot?.toLabel().orEmpty(),
                status = row.status.toReservationStatus(),
                totalPrice = row.totalPrice,
                currency = row.currency,
                note = row.notes,
                createdAt = row.createdAt.toInstantOrNow(),
                updatedAt = (row.updatedAt ?: row.createdAt).toInstantOrNow()
            )
        }
    }

    private suspend fun resolveOrganizerId(): String? =
        sessionLocalDataSource.authenticatedUserId.value?.takeIf { it.isNotBlank() }

    private fun String.toReservationStatus(): ReservationStatus =
        when (lowercase()) {
            "pending" -> ReservationStatus.PENDING
            "cancelled" -> ReservationStatus.CANCELLED
            else -> ReservationStatus.CONFIRMED
        }

    private fun ReservationStatus.toRemoteStatus(): String =
        when (this) {
            ReservationStatus.PENDING -> "pending"
            ReservationStatus.CONFIRMED -> "confirmed"
            ReservationStatus.CANCELLED -> "cancelled"
            ReservationStatus.COMPLETED -> "confirmed"
        }

    private fun TimeSlotRowDto.toLabel(): String {
        val zoneId = ZoneId.systemDefault()
        val start = startTime.toInstantOrNow().atZone(zoneId)
        val end = endTime.toInstantOrNow().atZone(zoneId)
        return "${start.format(slotStartFormatter)} - ${end.format(slotEndFormatter)}"
    }

    private fun estimateTotalPrice(venue: VenueRowDto, slot: TimeSlotRowDto): Double? {
        val hourlyRate = venue.pricePerHour ?: return null
        val durationHours = slot.durationHours().takeIf { hours -> hours > 0.0 } ?: return null
        return (round(hourlyRate * durationHours * 100.0) / 100.0)
    }

    private fun TimeSlotRowDto.durationHours(): Double {
        val startInstant = startTime.toInstantOrNow()
        val endInstant = endTime.toInstantOrNow()
        return (endInstant.toEpochMilli() - startInstant.toEpochMilli()) / 3_600_000.0
    }

    private fun String.toInstantOrNow(): Instant =
        runCatching { Instant.parse(this) }.getOrElse { Instant.now() }

    private companion object {
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM HH:mm")
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
