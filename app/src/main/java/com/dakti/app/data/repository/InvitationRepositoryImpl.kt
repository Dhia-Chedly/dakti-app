package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.InvitationDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Invitation
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InvitationRepositoryImpl @Inject constructor(
    private val invitationDao: InvitationDao,
    private val userDao: UserDao
) : InvitationRepository {

    override suspend fun getInvitations(): Resource<List<Invitation>> {
        ensureDemoPlayerProfile()
        val invitations = invitationDao.getInvitationsByPlayer(DEMO_PLAYER_ID)
            .map { entity -> entity.toDomain() }
        return Resource.Success(invitations)
    }

    override suspend fun respondToInvitation(invitationId: String, accepted: Boolean): Resource<Unit> {
        val status = if (accepted) {
            InvitationResponseStatus.ACCEPTED
        } else {
            InvitationResponseStatus.DECLINED
        }

        invitationDao.updateInvitationStatus(
            invitationId = invitationId,
            status = status,
            respondedAt = Instant.now().toEpochMilli()
        )

        return Resource.Success(Unit)
    }

    override fun observeInvitationsByPlayer(playerId: String): Flow<List<Invitation>> =
        invitationDao.observeInvitationsByPlayer(playerId)
            .map { entities -> entities.map { entity -> entity.toDomain() } }

    override fun observeInvitationsByMatch(matchId: String): Flow<List<Invitation>> =
        invitationDao.observeInvitationsByMatch(matchId)
            .map { entities -> entities.map { entity -> entity.toDomain() } }

    override suspend fun saveInvitation(invitation: Invitation): Resource<Invitation> {
        invitationDao.upsertInvitation(invitation.toEntity())
        return Resource.Success(invitation)
    }

    private suspend fun ensureDemoPlayerProfile() {
        val now = Instant.now()
        userDao.upsertUser(
            User(
                id = DEMO_PLAYER_ID,
                displayName = "Dakti Player",
                email = "player@dakti.app",
                phoneNumber = null,
                avatarUrl = null,
                role = UserRole.PLAYER,
                bio = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )

        userDao.upsertPlayer(
            Player(
                userId = DEMO_PLAYER_ID,
                preferredSport = "Football",
                availabilityNote = "Evenings",
                skillLevel = "Intermediate",
                rating = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    companion object {
        private const val DEMO_PLAYER_ID: String = "player-demo"
    }
}
