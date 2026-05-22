package com.dakti.app.data.repository

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import com.dakti.app.data.remote.supabase.model.InvitationRowDto
import com.dakti.app.data.remote.supabase.model.MatchRowDto
import com.dakti.app.data.remote.supabase.model.ReservationRowDto
import com.dakti.app.data.remote.supabase.model.TimeSlotRowDto
import com.dakti.app.data.remote.supabase.model.VenueRowDto
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchReservationContext
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val notificationRepository: NotificationRepository
) : MatchRepository {

    private val matchesCache = MutableStateFlow<Map<String, List<MatchWithContext>>>(emptyMap())

    override suspend fun getMyMatches(): Resource<List<MatchWithContext>> {
        val organizerId = resolveOrganizerId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val rows = supabaseRemoteDataSource.getMatchesByOrganizer(organizerId)
            val hydrated = hydrateMatches(rows)
            matchesCache.value = matchesCache.value + (organizerId to hydrated)
            Resource.Success(hydrated)
        }.getOrElse { error ->
            Resource.Error(error.toUserFacingMatchError("Could not fetch matches"))
        }
    }

    override suspend fun createMatch(payload: MatchCreatePayload): Resource<MatchWithContext> {
        val organizerId = resolveOrganizerId() ?: return Resource.Error("No authenticated user")
        if (payload.requiredPlayers < MIN_REQUIRED_PLAYERS) {
            return Resource.Error("A match requires at least $MIN_REQUIRED_PLAYERS players")
        }

        return runCatching {
            val venue = supabaseRemoteDataSource.getVenueById(payload.venueId)
                ?: return@runCatching Resource.Error("Selected venue is unavailable")

            if (!payload.reservationId.isNullOrBlank()) {
                val reservation = supabaseRemoteDataSource.getReservationById(payload.reservationId)
                    ?: return@runCatching Resource.Error("Linked reservation not found")
                if (reservation.organizerId != organizerId) {
                    return@runCatching Resource.Error("You can only use your own reservation")
                }
            }

            val created = supabaseRemoteDataSource.createMatch(
                payload = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "organizer_id" to organizerId,
                    "venue_id" to payload.venueId,
                    "reservation_id" to payload.reservationId,
                    "sport_type" to payload.sportType,
                    "match_time" to payload.scheduledStartTime.toString(),
                    "required_players" to payload.requiredPlayers,
                    "status" to "organizing",
                    "description" to payload.description
                )
            ) ?: return@runCatching Resource.Error("Could not create match")

            notificationRepository.scheduleMatchReminder(
                matchId = created.id,
                scheduledStartTime = created.matchTime.toInstantOrNow()
            )
            notificationRepository.scheduleMatchReadinessMonitoring(
                matchId = created.id,
                scheduledStartTime = created.matchTime.toInstantOrNow()
            )

            val details = hydrateMatches(listOf(created), mapOf(created.venueId to venue)).first()
            Resource.Success(details)
        }.getOrElse { error ->
            Resource.Error(error.toUserFacingMatchError("Could not create match"))
        }
    }

    override suspend fun getMatchDetails(matchId: String): Resource<MatchWithContext> {
        return runCatching {
            val row = supabaseRemoteDataSource.getMatchById(matchId)
                ?: return@runCatching Resource.Error("Match not found")
            val details = hydrateMatches(listOf(row)).firstOrNull()
                ?: return@runCatching Resource.Error("Match details unavailable")
            Resource.Success(details)
        }.getOrElse { error ->
            Resource.Error(error.toUserFacingMatchError("Could not fetch match details"))
        }
    }

    override suspend fun getReservationContextsForCurrentOrganizer(): Resource<List<MatchReservationContext>> {
        val organizerId = resolveOrganizerId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val reservations = supabaseRemoteDataSource.getReservationsByOrganizer(organizerId)
            val contexts = reservations.map { row ->
                val venue = runCatching {
                    supabaseRemoteDataSource.getVenueById(row.venueId)
                }.getOrNull()
                val slot = runCatching {
                    supabaseRemoteDataSource.getTimeSlotById(row.timeSlotId)
                }.getOrNull()
                MatchReservationContext(
                    reservationId = row.id,
                    venueId = row.venueId,
                    venueName = venue?.name.orEmpty(),
                    venueAddress = venue?.address.orEmpty(),
                    reservationStatus = row.status.toReservationStatus(),
                    sportType = venue?.sportType.orEmpty(),
                    scheduledStartTime = slot?.startTime?.toInstantOrNow()
                        ?: row.createdAt.toInstantOrNow(),
                    timeSlotLabel = slot?.toLabel() ?: "Reserved slot"
                )
            }
            Resource.Success(contexts)
        }.getOrElse { error ->
            Resource.Error(error.toUserFacingMatchError("Could not fetch reservation contexts"))
        }
    }

    override suspend fun updateMatchStatus(
        matchId: String,
        status: MatchStatus
    ): Resource<Unit> {
        return runCatching {
            val existing = supabaseRemoteDataSource.getMatchById(matchId)
                ?: return@runCatching Resource.Error("Match not found")

            supabaseRemoteDataSource.updateMatch(
                matchId = matchId,
                payload = mapOf(
                    "status" to status.toRemoteStatus(),
                    "updated_at" to Instant.now().toString()
                )
            )

            notificationRepository.sendMatchUpdatedNotification(
                matchId = matchId,
                updateMessage = "Match status updated to ${status.toDisplayLabel()}."
            )

            if (status == MatchStatus.CANCELLED || status == MatchStatus.COMPLETED) {
                notificationRepository.cancelMatchReminder(matchId)
                notificationRepository.cancelMatchReadinessMonitoring(matchId)
            } else {
                val startTime = existing.matchTime.toInstantOrNow()
                notificationRepository.scheduleMatchReminder(matchId, startTime)
                notificationRepository.scheduleMatchReadinessMonitoring(matchId, startTime)
            }

            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.toUserFacingMatchError("Could not update match status"))
        }
    }

    override fun observeMatchesByOrganizer(organizerId: String): Flow<List<Match>> =
        matchesCache
            .asStateFlow()
            .map { cache -> cache[organizerId].orEmpty().map { item -> item.match } }

    override fun observeMatchWithInvitations(matchId: String): Flow<MatchWithInvitations?> =
        matchesCache
            .asStateFlow()
            .map { cache ->
                val match = cache.values.flatten().firstOrNull { item -> item.match.id == matchId }?.match
                match?.let { MatchWithInvitations(match = it, invitations = emptyList()) }
            }

    override suspend fun saveMatch(match: Match): Resource<Match> {
        return runCatching {
            val payload = mapOf(
                "id" to match.id,
                "organizer_id" to match.organizerId,
                "venue_id" to match.venueId,
                "reservation_id" to match.reservationId,
                "sport_type" to match.sportType,
                "match_time" to match.scheduledStartTime.toString(),
                "required_players" to match.requiredPlayers,
                "status" to match.status.toRemoteStatus(),
                "description" to match.description,
                "updated_at" to Instant.now().toString()
            )

            if (supabaseRemoteDataSource.getMatchById(match.id) == null) {
                supabaseRemoteDataSource.createMatch(payload)
            } else {
                supabaseRemoteDataSource.updateMatch(match.id, payload)
            }
            Resource.Success(match)
        }.getOrElse { error ->
            Resource.Error(error.toUserFacingMatchError("Could not save match"))
        }
    }

    private suspend fun hydrateMatches(
        rows: List<MatchRowDto>,
        venueCache: Map<String, VenueRowDto?> = emptyMap()
    ): List<MatchWithContext> {
        if (rows.isEmpty()) {
            return emptyList()
        }

        val venuesById = rows.map { row -> row.venueId }.distinct().associateWith { id ->
            venueCache[id] ?: supabaseRemoteDataSource.getVenueById(id)
        }

        val invitationsByMatch = rows.associate { row ->
            row.id to supabaseRemoteDataSource.getInvitationsByMatch(row.id)
        }

        return rows.map { row ->
            val venue = venuesById[row.venueId]
            val invitations = invitationsByMatch[row.id].orEmpty()
            row.toDomainWithContext(
                venue = venue,
                invitations = invitations
            )
        }
    }

    private fun MatchRowDto.toDomainWithContext(
        venue: VenueRowDto?,
        invitations: List<InvitationRowDto>
    ): MatchWithContext {
        val match = Match(
            id = id,
            organizerId = organizerId,
            venueId = venueId,
            reservationId = reservationId,
            title = title?.takeIf { value -> value.isNotBlank() } ?: "$sportType Match",
            sportType = sportType,
            scheduledStartTime = matchTime.toInstantOrNow(),
            requiredPlayers = requiredPlayers,
            status = status.toMatchStatus(),
            description = description,
            createdAt = createdAt.toInstantOrNow(),
            updatedAt = updatedAt.toInstantOrNow()
        )

        val confirmed = invitations.count { invitation -> invitation.responseStatus.equals("accepted", ignoreCase = true) }
        val pending = invitations.count { invitation -> invitation.responseStatus.equals("pending", ignoreCase = true) }
        val declined = invitations.count { invitation -> invitation.responseStatus.equals("declined", ignoreCase = true) }

        return MatchWithContext(
            match = match,
            venueName = venue?.name.orEmpty(),
            venueAddress = venue?.address.orEmpty(),
            reservationReference = reservationId,
            organizerName = null,
            invitedPlayersCount = invitations.size,
            confirmedPlayersCount = confirmed,
            pendingPlayersCount = pending,
            declinedPlayersCount = declined
        )
    }

    private suspend fun resolveOrganizerId(): String? =
        sessionLocalDataSource.authenticatedUserId.value?.takeIf { it.isNotBlank() }

    private fun String.toMatchStatus(): MatchStatus =
        when (lowercase()) {
            "confirmed" -> MatchStatus.CONFIRMED
            "cancelled" -> MatchStatus.CANCELLED
            "completed" -> MatchStatus.COMPLETED
            else -> MatchStatus.ORGANIZING
        }

    private fun MatchStatus.toRemoteStatus(): String =
        when (this) {
            MatchStatus.ORGANIZING,
            MatchStatus.DRAFT,
            MatchStatus.OPEN -> "organizing"
            MatchStatus.CONFIRMED,
            MatchStatus.FULL -> "confirmed"
            MatchStatus.CANCELLED -> "cancelled"
            MatchStatus.COMPLETED -> "completed"
        }

    private fun MatchStatus.toDisplayLabel(): String =
        when (this) {
            MatchStatus.ORGANIZING,
            MatchStatus.DRAFT,
            MatchStatus.OPEN -> "organizing"
            MatchStatus.CONFIRMED,
            MatchStatus.FULL -> "confirmed"
            MatchStatus.CANCELLED -> "cancelled"
            MatchStatus.COMPLETED -> "completed"
        }

    private fun String.toReservationStatus(): ReservationStatus =
        when (lowercase()) {
            "pending" -> ReservationStatus.PENDING
            "cancelled" -> ReservationStatus.CANCELLED
            "completed" -> ReservationStatus.COMPLETED
            else -> ReservationStatus.CONFIRMED
        }

    private fun TimeSlotRowDto.toLabel(): String {
        val zoneId = ZoneId.systemDefault()
        val start = startTime.toInstantOrNow().atZone(zoneId)
        val end = endTime.toInstantOrNow().atZone(zoneId)
        return "${start.format(slotStartFormatter)} - ${end.format(slotEndFormatter)}"
    }

    private fun String.toInstantOrNow(): Instant =
        runCatching { Instant.parse(this) }.getOrElse { Instant.now() }

    private fun Throwable.toUserFacingMatchError(defaultMessage: String): String {
        if (this !is HttpException) {
            return message ?: defaultMessage
        }

        val body = runCatching {
            response()?.errorBody()?.string()
        }.getOrNull()?.takeIf { value -> value.isNotBlank() }

        return if (body != null) {
            "HTTP ${code()}: $body"
        } else {
            message ?: defaultMessage
        }
    }

    private companion object {
        private const val MIN_REQUIRED_PLAYERS: Int = 2
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM HH:mm")
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
