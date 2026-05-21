package com.dakti.app.data.repository

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import com.dakti.app.data.remote.supabase.model.InvitationRowDto
import com.dakti.app.data.remote.supabase.model.MatchRowDto
import com.dakti.app.data.remote.supabase.model.ProfileRowDto
import com.dakti.app.data.remote.supabase.model.VenueRowDto
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.InvitePlayerCandidate
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvitationRepositoryImpl @Inject constructor(
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource
) : InvitationRepository {

    override suspend fun getInvitationsForCurrentPlayer(): Resource<List<InvitationWithContext>> {
        val currentUserId = resolveAuthenticatedUserId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val rows = supabaseRemoteDataSource.getInvitationsByPlayer(currentUserId)
            Resource.Success(hydrateInvitations(rows))
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch invitations")
        }
    }

    override suspend fun getInvitationsForMatch(matchId: String): Resource<List<InvitationWithContext>> {
        val currentUserId = resolveAuthenticatedUserId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val match = supabaseRemoteDataSource.getMatchById(matchId)
                ?: return@runCatching Resource.Error("Match not found")
            if (match.organizerId != currentUserId) {
                return@runCatching Resource.Error("Only the organizer can view match invitations")
            }

            val rows = supabaseRemoteDataSource.getInvitationsByMatch(matchId)
            Resource.Success(hydrateInvitations(rows))
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch match invitations")
        }
    }

    override suspend fun getInviteCandidates(matchId: String): Resource<List<InvitePlayerCandidate>> {
        val currentUserId = resolveAuthenticatedUserId() ?: return Resource.Error("No authenticated user")

        return runCatching {
            val match = supabaseRemoteDataSource.getMatchById(matchId)
                ?: return@runCatching Resource.Error("Match not found")
            if (match.organizerId != currentUserId) {
                return@runCatching Resource.Error("Only organizer can invite players")
            }

            val existing = supabaseRemoteDataSource.getInvitationsByMatch(matchId)
                .associateBy { invitation -> invitation.playerId }

            val profiles = supabaseRemoteDataSource.selectProfiles(
                filters = mapOf("role" to "eq.player")
            )

            val candidates = profiles
                .filterNot { profile -> profile.id == currentUserId || profile.id == match.organizerId }
                .map { profile ->
                    val existingInvite = existing[profile.id]
                    InvitePlayerCandidate(
                        playerId = profile.id,
                        displayName = profile.fullName,
                        email = profile.email,
                        phoneNumber = profile.phone,
                        preferredSport = profile.preferredSport.orEmpty(),
                        availabilityNote = profile.availabilityNote,
                        skillLevel = null,
                        invitationStatus = existingInvite?.responseStatus?.toInvitationStatus()
                    )
                }
            Resource.Success(candidates)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not load invite candidates")
        }
    }

    override suspend fun invitePlayers(
        matchId: String,
        playerIds: List<String>,
        message: String?
    ): Resource<Int> {
        val organizerId = resolveAuthenticatedUserId() ?: return Resource.Error("No authenticated user")
        val uniquePlayerIds = playerIds.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (uniquePlayerIds.isEmpty()) {
            return Resource.Error("Select at least one player")
        }

        return runCatching {
            val match = supabaseRemoteDataSource.getMatchById(matchId)
                ?: return@runCatching Resource.Error("Match not found")
            if (match.organizerId != organizerId) {
                return@runCatching Resource.Error("Only organizer can send invitations")
            }

            val existing = supabaseRemoteDataSource.getInvitationsByMatchAndPlayers(
                matchId = matchId,
                playerIds = uniquePlayerIds
            ).associateBy { invitation -> invitation.playerId }

            val payload = uniquePlayerIds
                .filterNot { playerId -> existing.containsKey(playerId) }
                .map { playerId ->
                    mapOf(
                        "match_id" to matchId,
                        "player_id" to playerId,
                        "sender_id" to organizerId,
                        "message_text" to message?.trim()?.ifBlank { null },
                        "response_status" to "pending"
                    )
                }

            if (payload.isEmpty()) {
                return@runCatching Resource.Success(0)
            }

            val inserted = supabaseRemoteDataSource.createInvitations(payload)
            Resource.Success(inserted.size)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not send invitations")
        }
    }

    override suspend fun respondToInvitation(
        invitationId: String,
        status: InvitationResponseStatus
    ): Resource<Unit> {
        val currentUserId = resolveAuthenticatedUserId() ?: return Resource.Error("No authenticated user")
        if (status == InvitationResponseStatus.EXPIRED) {
            return Resource.Error("Invalid response status")
        }

        return runCatching {
            val invitation = supabaseRemoteDataSource.getInvitationById(invitationId)
                ?: return@runCatching Resource.Error("Invitation not found")
            if (invitation.playerId != currentUserId) {
                return@runCatching Resource.Error("You can only respond to your own invitations")
            }

            supabaseRemoteDataSource.updateInvitation(
                invitationId = invitationId,
                payload = mapOf(
                    "response_status" to status.toRemoteStatus(),
                    "responded_at" to if (status == InvitationResponseStatus.PENDING) {
                        null
                    } else {
                        Instant.now().toString()
                    }
                )
            )
            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not update invitation")
        }
    }

    private suspend fun hydrateInvitations(rows: List<InvitationRowDto>): List<InvitationWithContext> {
        if (rows.isEmpty()) {
            return emptyList()
        }

        val matchIds = rows.map { row -> row.matchId }.distinct()
        val matches = matchIds.associateWith { id -> supabaseRemoteDataSource.getMatchById(id) }

        val venueIds = matches.values.mapNotNull { match -> match?.venueId }.distinct()
        val venues = venueIds.associateWith { id -> supabaseRemoteDataSource.getVenueById(id) }

        val profileIds = buildSet {
            rows.forEach { row ->
                add(row.playerId)
                add(row.senderId)
            }
            matches.values.forEach { match ->
                match?.organizerId?.let { add(it) }
            }
        }
        val profiles = profileIds.associateWith { id -> supabaseRemoteDataSource.getProfile(id) }

        return rows.mapNotNull { row ->
            val match = matches[row.matchId] ?: return@mapNotNull null
            val venue = venues[match.venueId]
            val player = profiles[row.playerId]
            val sender = profiles[row.senderId]

            InvitationWithContext(
                invitationId = row.id,
                matchId = row.matchId,
                playerId = row.playerId,
                playerName = player?.fullName.orEmpty(),
                organizerId = row.senderId,
                organizerName = sender?.fullName.orEmpty(),
                matchTitle = match.title.orEmpty(),
                sportType = match.sportType,
                venueName = venue?.name.orEmpty(),
                venueAddress = venue?.address.orEmpty(),
                scheduledStartTime = match.matchTime.toInstantOrNow(),
                requiredPlayers = match.requiredPlayers,
                status = row.responseStatus.toInvitationStatus(),
                message = row.messageText,
                sentAt = row.sentAt.toInstantOrNow(),
                respondedAt = row.respondedAt?.toInstantOrNow()
            )
        }
    }

    private fun String.toInvitationStatus(): InvitationResponseStatus =
        when (lowercase()) {
            "accepted" -> InvitationResponseStatus.ACCEPTED
            "declined" -> InvitationResponseStatus.DECLINED
            "expired" -> InvitationResponseStatus.EXPIRED
            else -> InvitationResponseStatus.PENDING
        }

    private fun InvitationResponseStatus.toRemoteStatus(): String =
        when (this) {
            InvitationResponseStatus.PENDING -> "pending"
            InvitationResponseStatus.ACCEPTED -> "accepted"
            InvitationResponseStatus.DECLINED -> "declined"
            InvitationResponseStatus.EXPIRED -> "declined"
        }

    private fun String.toInstantOrNow(): Instant =
        runCatching { Instant.parse(this) }.getOrElse { Instant.now() }

    private fun resolveAuthenticatedUserId(): String? =
        sessionLocalDataSource.authenticatedUserId.value?.takeIf { it.isNotBlank() }
}
