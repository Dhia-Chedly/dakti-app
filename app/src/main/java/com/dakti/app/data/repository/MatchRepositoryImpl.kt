package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.MatchDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val venueDao: VenueDao,
    private val userDao: UserDao
) : MatchRepository {

    override suspend fun getMyMatches(): Resource<List<Match>> {
        ensureDemoOrganizerProfile()
        return Resource.Success(
            matchDao.getMatchesByOrganizer(DEMO_ORGANIZER_ID)
                .map { entity -> entity.toDomain() }
        )
    }

    override suspend fun createMatch(title: String): Resource<Match> {
        ensureDemoOrganizerProfile()
        ensureVenueExists()

        val venue = venueDao.getVenuesOnce().firstOrNull()
            ?: return Resource.Error("Create a venue before creating matches")

        val now = Instant.now()
        val match = Match(
            id = "match-${UUID.randomUUID()}",
            organizerId = DEMO_ORGANIZER_ID,
            venueId = venue.id,
            reservationId = null,
            title = title,
            sportType = venue.sportType,
            scheduledStartTime = now.plusSeconds(24L * 3600L),
            requiredPlayers = 10,
            status = MatchStatus.DRAFT,
            description = null,
            createdAt = now,
            updatedAt = now
        )

        matchDao.upsertMatch(match.toEntity())
        return Resource.Success(match)
    }

    override suspend fun getMatchDetails(matchId: String): Resource<Match> {
        val match = matchDao.getMatchById(matchId)?.toDomain()
            ?: return Resource.Error("Match not found")
        return Resource.Success(match)
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
                bio = null,
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

    private suspend fun ensureVenueExists() {
        if (venueDao.getVenuesOnce().isNotEmpty()) {
            return
        }

        val now = Instant.now()
        venueDao.upsertVenue(
            Venue(
                id = "venue-default",
                name = "Dakti Demo Arena",
                sportType = "Football",
                description = "Seed venue for early development flows.",
                address = "1 Demo Street",
                city = "Lagos",
                state = "Lagos",
                country = "Nigeria",
                latitude = null,
                longitude = null,
                pricePerHour = 10000.0,
                currency = "NGN",
                amenities = emptyList(),
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    companion object {
        private const val DEMO_ORGANIZER_ID: String = "organizer-demo"
    }
}
