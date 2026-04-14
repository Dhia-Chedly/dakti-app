package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.model.UserWithProfiles
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return Resource.Success(existing.toDomain())
        }

        val now = Instant.now()
        val user = User(
            id = "usr-${UUID.randomUUID()}",
            displayName = defaultDisplayName(email = email),
            email = email,
            phoneNumber = null,
            avatarUrl = null,
            role = UserRole.PLAYER,
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        userDao.upsertUser(user.toEntity())
        userDao.upsertPlayer(
            Player(
                userId = user.id,
                preferredSport = "Football",
                availabilityNote = "Weekends",
                skillLevel = "Intermediate",
                rating = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )

        return Resource.Success(user)
    }

    override suspend fun register(name: String, email: String, password: String): Resource<User> {
        val now = Instant.now()
        val user = User(
            id = "usr-${UUID.randomUUID()}",
            displayName = name,
            email = email,
            phoneNumber = null,
            avatarUrl = null,
            role = UserRole.BOTH,
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        userDao.upsertUser(user.toEntity())
        userDao.upsertPlayer(
            Player(
                userId = user.id,
                preferredSport = "Football",
                availabilityNote = null,
                skillLevel = null,
                rating = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )

        return Resource.Success(user)
    }

    override suspend fun logout(): Resource<Unit> = Resource.Success(Unit)

    override suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)?.toDomain()

    override fun observeUser(userId: String): Flow<User?> =
        userDao.observeUserById(userId).map { entity -> entity?.toDomain() }

    override suspend fun getUserWithProfiles(userId: String): UserWithProfiles? =
        userDao.getUserWithProfiles(userId)?.toDomain()

    override suspend fun upsertOrganizerProfile(organizer: Organizer): Resource<Unit> {
        userDao.upsertOrganizer(organizer.toEntity())
        return Resource.Success(Unit)
    }

    override suspend fun upsertPlayerProfile(player: Player): Resource<Unit> {
        userDao.upsertPlayer(player.toEntity())
        return Resource.Success(Unit)
    }

    private fun defaultDisplayName(email: String): String {
        val handle = email.substringBefore("@").trim()
        return handle.ifEmpty { "Dakti Player" }
    }
}
