package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.MatchDao
import com.dakti.app.data.local.dao.ReservationDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchReservationContext
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val reservationDao: ReservationDao,
    private val venueDao: VenueDao,
    private val userDao: UserDao,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val notificationRepository: NotificationRepository
) : MatchRepository {

    override suspend fun getMyMatches(): Resource<List<MatchWithContext>> {
        val organizerId = resolveOrganizerId()
        val organizerName = userDao.getUserById(organizerId)?.displayName
        val matches = matchDao.getMatchesWithContextByOrganizer(organizerId)
            .map { relation -> relation.toDomain(organizerName = organizerName) }
        return Resource.Success(matches)
    }

    override suspend fun createMatch(payload: MatchCreatePayload): Resource<MatchWithContext> {
        val organizerId = resolveOrganizerId()
        if (payload.requiredPlayers < MIN_REQUIRED_PLAYERS) {
            return Resource.Error("A match requires at least $MIN_REQUIRED_PLAYERS players")
        }

        val venue = venueDao.getVenueById(payload.venueId)
            ?: return Resource.Error("Selected venue is no longer available")

        val reservation = payload.reservationId?.let { reservationId ->
            val reservationDetails = reservationDao.getReservationWithDetails(reservationId)
                ?: return Resource.Error("Linked reservation not found")
            if (reservationDetails.reservation.organizerId != organizerId) {
                return Resource.Error("You can only create matches from your own reservations")
            }
            if (reservationDetails.reservation.venueId != payload.venueId) {
                return Resource.Error("Reservation does not belong to selected venue")
            }
            reservationDetails
        }

        val now = Instant.now()
        val match = Match(
            id = "match-${UUID.randomUUID()}",
            organizerId = organizerId,
            venueId = venue.id,
            reservationId = reservation?.reservation?.id,
            title = "${payload.sportType} Match",
            sportType = payload.sportType,
            scheduledStartTime = payload.scheduledStartTime,
            requiredPlayers = payload.requiredPlayers,
            status = MatchStatus.ORGANIZING,
            description = payload.description?.trim()?.takeIf { value -> value.isNotBlank() },
            createdAt = now,
            updatedAt = now
        )

        matchDao.upsertMatch(match.toEntity())
        return getMatchDetails(match.id)
    }

    override suspend fun getMatchDetails(matchId: String): Resource<MatchWithContext> {
        val relation = matchDao.getMatchWithContextById(matchId)
            ?: return Resource.Error("Match not found")
        val organizerName = userDao.getUserById(relation.match.organizerId)?.displayName
        return Resource.Success(relation.toDomain(organizerName = organizerName))
    }

    override suspend fun getReservationContextsForCurrentOrganizer(): Resource<List<MatchReservationContext>> {
        val organizerId = resolveOrganizerId()
        val reservations = reservationDao.getReservationsWithDetailsByOrganizer(organizerId)
            .map { relation ->
                MatchReservationContext(
                    reservationId = relation.reservation.id,
                    venueId = relation.venue.id,
                    venueName = relation.venue.name,
                    venueAddress = relation.venue.address,
                    sportType = relation.venue.sportType,
                    scheduledStartTime = Instant.ofEpochMilli(relation.timeSlot.startTime),
                    timeSlotLabel = formatTimeSlot(
                        startMillis = relation.timeSlot.startTime,
                        endMillis = relation.timeSlot.endTime
                    )
                )
            }
        return Resource.Success(reservations)
    }

    override suspend fun updateMatchStatus(
        matchId: String,
        status: MatchStatus
    ): Resource<Unit> {
        val match = matchDao.getMatchById(matchId)
            ?: return Resource.Error("Match not found")
        matchDao.updateMatch(
            match.copy(
                status = status,
                updatedAt = Instant.now().toEpochMilli()
            )
        )

        notificationRepository.sendMatchUpdatedNotification(
            matchId = matchId,
            updateMessage = "Match status updated to ${status.toDisplayLabel()}."
        )

        if (status == MatchStatus.CANCELLED || status == MatchStatus.COMPLETED) {
            notificationRepository.cancelMatchReminder(matchId)
        } else {
            notificationRepository.scheduleMatchReminder(
                matchId = matchId,
                scheduledStartTime = Instant.ofEpochMilli(match.scheduledStartTime)
            )
        }

        return Resource.Success(Unit)
    }

    override fun observeMatchesByOrganizer(organizerId: String): Flow<List<Match>> =
        matchDao.observeMatchesByOrganizer(organizerId)
            .map { entities -> entities.map { entity -> entity.toDomain() } }

    override fun observeMatchWithInvitations(matchId: String): Flow<MatchWithInvitations?> =
        matchDao.observeMatchWithInvitations(matchId)
            .map { relation -> relation?.toDomain() }

    override suspend fun saveMatch(match: Match): Resource<Match> {
        matchDao.upsertMatch(match.toEntity())
        return Resource.Success(match)
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
                rating = 4.7,
                totalHostedMatches = 0,
                organizationName = "Dakti Hosts",
                isVerified = true,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    private fun formatTimeSlot(startMillis: Long, endMillis: Long): String {
        val zoneId = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val end = Instant.ofEpochMilli(endMillis).atZone(zoneId)
        return "${start.format(slotStartFormatter)} - ${end.format(slotEndFormatter)}"
    }

    companion object {
        private const val DEMO_ORGANIZER_ID: String = "organizer-demo"
        private const val MIN_REQUIRED_PLAYERS: Int = 2
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM HH:mm")
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
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
}
