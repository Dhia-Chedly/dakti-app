package com.dakti.app.data.repository

import androidx.room.withTransaction
import com.dakti.app.data.local.dao.InvitationDao
import com.dakti.app.data.local.dao.MatchDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.database.AppDatabase
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Invitation
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.InvitePlayerCandidate
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor(
    private val invitationDao: InvitationDao,
    private val matchDao: MatchDao,
    private val userDao: UserDao,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val appDatabase: AppDatabase
) : InvitationRepository {

    override suspend fun getInvitationsForCurrentPlayer(): Resource<List<InvitationWithContext>> {
        ensureDemoPlayersSeeded()
        val currentUserId = resolveAuthenticatedUserId()
            ?: return Resource.Error("No authenticated user")
        ensurePlayerProfileForUser(currentUserId)
        val invitations = invitationDao.getInvitationsByPlayer(currentUserId)
        return Resource.Success(buildInvitationContexts(invitations))
    }

    override suspend fun getInvitationsForMatch(matchId: String): Resource<List<InvitationWithContext>> {
        val match = matchDao.getMatchById(matchId) ?: return Resource.Error("Match not found")
        val currentUserId = resolveAuthenticatedUserId()
        if (currentUserId != null && match.organizerId != currentUserId) {
            return Resource.Error("Only the organizer can view match invitations")
        }
        val invitations = invitationDao.getInvitationsByMatch(matchId)
        return Resource.Success(buildInvitationContexts(invitations))
    }

    override suspend fun getInviteCandidates(matchId: String): Resource<List<InvitePlayerCandidate>> {
        ensureDemoPlayersSeeded()
        val match = matchDao.getMatchById(matchId) ?: return Resource.Error("Match not found")
        val currentUserId = resolveAuthenticatedUserId()
        if (currentUserId != null && match.organizerId != currentUserId) {
            return Resource.Error("Only the organizer can invite players")
        }

        val existingInvitations = invitationDao.getInvitationsByMatch(matchId)
            .associateBy { invitation -> invitation.playerId }

        val excludedUserIds = buildSet {
            add(match.organizerId)
            if (!currentUserId.isNullOrBlank()) {
                add(currentUserId)
            }
        }

        val candidates = userDao.getAllUsersWithProfiles()
            .asSequence()
            .filter { relation -> relation.player != null }
            .filterNot { relation -> relation.user.id in excludedUserIds }
            .map { relation ->
                InvitePlayerCandidate(
                    playerId = relation.user.id,
                    displayName = relation.user.displayName,
                    email = relation.user.email,
                    phoneNumber = relation.user.phoneNumber,
                    preferredSport = relation.player?.preferredSport.orEmpty(),
                    availabilityNote = relation.player?.availabilityNote,
                    skillLevel = relation.player?.skillLevel,
                    invitationStatus = existingInvitations[relation.user.id]?.status
                )
            }
            .toList()

        return Resource.Success(candidates)
    }

    override suspend fun invitePlayers(
        matchId: String,
        playerIds: List<String>,
        message: String?
    ): Resource<Int> {
        val normalizedPlayerIds = playerIds
            .map { playerId -> playerId.trim() }
            .filter { playerId -> playerId.isNotBlank() }
            .distinct()

        if (normalizedPlayerIds.isEmpty()) {
            return Resource.Error("Select at least one player")
        }

        val organizerId = resolveAuthenticatedUserId()
            ?: return Resource.Error("No authenticated user")
        val match = matchDao.getMatchById(matchId) ?: return Resource.Error("Match not found")
        if (match.organizerId != organizerId) {
            return Resource.Error("Only the organizer can invite players for this match")
        }

        ensureDemoPlayersSeeded()
        val usersWithProfilesById = userDao.getAllUsersWithProfiles()
            .associateBy { relation -> relation.user.id }
        val invalidTarget = normalizedPlayerIds.firstOrNull { playerId ->
            usersWithProfilesById[playerId]?.player == null
        }
        if (invalidTarget != null) {
            return Resource.Error("One or more selected users are not player accounts")
        }

        val existingInvitations = invitationDao.getInvitationsByMatchAndPlayers(
            matchId = matchId,
            playerIds = normalizedPlayerIds
        ).associateBy { invitation -> invitation.playerId }

        val nowMillis = Instant.now().toEpochMilli()
        val messageText = message?.trim()?.takeIf { value -> value.isNotBlank() }

        val newInvitations = normalizedPlayerIds
            .filterNot { playerId -> existingInvitations.containsKey(playerId) }
            .map { playerId ->
                Invitation(
                    id = "inv-${UUID.randomUUID()}",
                    matchId = matchId,
                    playerId = playerId,
                    invitedByOrganizerId = organizerId,
                    matchTitle = match.title,
                    fromUser = organizerId,
                    status = InvitationResponseStatus.PENDING,
                    message = messageText,
                    sentAt = Instant.ofEpochMilli(nowMillis),
                    respondedAt = null
                ).toEntity()
            }

        if (newInvitations.isEmpty()) {
            return Resource.Success(0)
        }

        return try {
            appDatabase.withTransaction {
                invitationDao.upsertInvitations(newInvitations)
            }
            Resource.Success(newInvitations.size)
        } catch (exception: Exception) {
            Resource.Error("Failed to send invitations")
        }
    }

    override suspend fun respondToInvitation(
        invitationId: String,
        status: InvitationResponseStatus
    ): Resource<Unit> {
        if (status == InvitationResponseStatus.EXPIRED) {
            return Resource.Error("Invalid response status")
        }

        val currentUserId = resolveAuthenticatedUserId()
            ?: return Resource.Error("No authenticated user")
        val invitation = invitationDao.getInvitationById(invitationId)
            ?: return Resource.Error("Invitation not found")

        if (invitation.playerId != currentUserId) {
            return Resource.Error("You can only respond to your own invitations")
        }

        val respondedAt = if (status == InvitationResponseStatus.PENDING) {
            null
        } else {
            Instant.now().toEpochMilli()
        }

        invitationDao.updateInvitationStatus(
            invitationId = invitationId,
            status = status,
            respondedAt = respondedAt
        )

        return Resource.Success(Unit)
    }

    private suspend fun buildInvitationContexts(
        invitations: List<com.dakti.app.data.local.entity.InvitationEntity>
    ): List<InvitationWithContext> {
        if (invitations.isEmpty()) {
            return emptyList()
        }

        val matchIds = invitations.map { invitation -> invitation.matchId }.distinct()
        val matchesById = if (matchIds.isEmpty()) {
            emptyMap()
        } else {
            matchDao.getMatchesWithContextByIds(matchIds)
                .associateBy { relation -> relation.match.id }
        }

        val relatedUserIds = buildSet {
            invitations.forEach { invitation ->
                add(invitation.playerId)
                invitation.invitedByOrganizerId?.let { organizerId ->
                    add(organizerId)
                }
            }
            matchesById.values.forEach { relation ->
                add(relation.match.organizerId)
            }
        }

        val usersById = if (relatedUserIds.isEmpty()) {
            emptyMap()
        } else {
            userDao.getUsersByIds(relatedUserIds.toList())
                .associateBy { user -> user.id }
        }

        return invitations.mapNotNull { invitation ->
            val relation = matchesById[invitation.matchId] ?: return@mapNotNull null
            val organizerId = invitation.invitedByOrganizerId ?: relation.match.organizerId
            val organizerName = usersById[organizerId]?.displayName ?: "Organizer"
            val playerName = usersById[invitation.playerId]?.displayName ?: "Player"

            InvitationWithContext(
                invitationId = invitation.id,
                matchId = invitation.matchId,
                playerId = invitation.playerId,
                playerName = playerName,
                organizerId = organizerId,
                organizerName = organizerName,
                matchTitle = relation.match.title,
                sportType = relation.match.sportType,
                venueName = relation.venue.name,
                venueAddress = relation.venue.address,
                scheduledStartTime = Instant.ofEpochMilli(relation.match.scheduledStartTime),
                requiredPlayers = relation.match.requiredPlayers,
                status = invitation.status,
                message = invitation.message,
                sentAt = Instant.ofEpochMilli(invitation.sentAt),
                respondedAt = invitation.respondedAt?.let { millis -> Instant.ofEpochMilli(millis) }
            )
        }
    }

    private suspend fun ensurePlayerProfileForUser(userId: String) {
        val withProfiles = userDao.getUserWithProfiles(userId)
        if (withProfiles?.player != null) {
            return
        }

        val now = Instant.now()
        userDao.upsertPlayer(
            Player(
                userId = userId,
                preferredSport = "Football",
                availabilityNote = "Evenings",
                skillLevel = "Intermediate",
                rating = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    private suspend fun ensureDemoPlayersSeeded() {
        val now = Instant.now()
        demoPlayers.forEach { seed ->
            if (userDao.getUserById(seed.id) == null) {
                userDao.upsertUser(
                    User(
                        id = seed.id,
                        displayName = seed.displayName,
                        email = seed.email,
                        phoneNumber = seed.phoneNumber,
                        avatarUrl = null,
                        role = UserRole.PLAYER,
                        bio = seed.bio,
                        createdAt = now,
                        updatedAt = now
                    ).toEntity()
                )
            }

            if (userDao.getUserWithProfiles(seed.id)?.player == null) {
                userDao.upsertPlayer(
                    Player(
                        userId = seed.id,
                        preferredSport = seed.preferredSport,
                        availabilityNote = seed.availabilityNote,
                        skillLevel = seed.skillLevel,
                        rating = null,
                        createdAt = now,
                        updatedAt = now
                    ).toEntity()
                )
            }
        }
    }

    private fun resolveAuthenticatedUserId(): String? {
        return sessionLocalDataSource.authenticatedUserId.value?.takeIf { it.isNotBlank() }
    }

    private data class DemoPlayerSeed(
        val id: String,
        val displayName: String,
        val email: String,
        val phoneNumber: String,
        val bio: String,
        val preferredSport: String,
        val availabilityNote: String,
        val skillLevel: String
    )

    private companion object {
        val demoPlayers: List<DemoPlayerSeed> = listOf(
            DemoPlayerSeed(
                id = "player-ada",
                displayName = "Ada Nwosu",
                email = "ada@dakti.app",
                phoneNumber = "+2348010000001",
                bio = "Defender and team motivator.",
                preferredSport = "Football",
                availabilityNote = "Weekdays after 6PM",
                skillLevel = "Intermediate"
            ),
            DemoPlayerSeed(
                id = "player-kareem",
                displayName = "Kareem Bello",
                email = "kareem@dakti.app",
                phoneNumber = "+2348010000002",
                bio = "Midfielder with strong passing game.",
                preferredSport = "Football",
                availabilityNote = "Weekends and Friday nights",
                skillLevel = "Advanced"
            ),
            DemoPlayerSeed(
                id = "player-zainab",
                displayName = "Zainab Adeyemi",
                email = "zainab@dakti.app",
                phoneNumber = "+2348010000003",
                bio = "Fast winger, available for mixed games.",
                preferredSport = "Football",
                availabilityNote = "Weeknights",
                skillLevel = "Intermediate"
            ),
            DemoPlayerSeed(
                id = "player-ifeanyi",
                displayName = "Ifeanyi Okeke",
                email = "ifeanyi@dakti.app",
                phoneNumber = "+2348010000004",
                bio = "Flexible player for football and basketball.",
                preferredSport = "Basketball",
                availabilityNote = "Saturday mornings",
                skillLevel = "Beginner"
            )
        )
    }
}
