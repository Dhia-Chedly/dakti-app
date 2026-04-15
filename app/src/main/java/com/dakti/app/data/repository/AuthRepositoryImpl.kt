package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.session.SessionLocalDataSource
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val sessionLocalDataSource: SessionLocalDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        val normalizedEmail = normalizeEmail(email)
        if (normalizedEmail.isBlank()) {
            return Resource.Error("Email is required")
        }

        if (normalizedEmail == DEMO_USER_EMAIL && password == DEMO_USER_PASSWORD) {
            val demoUser = ensureDemoUser()
            sessionLocalDataSource.setAuthenticatedUserId(demoUser.id)
            return Resource.Success(demoUser)
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            return Resource.Error("Invalid credentials")
        }

        val existingUser = userDao.getUserByEmail(normalizedEmail)
            ?: return Resource.Error("No account found for this email")

        sessionLocalDataSource.setAuthenticatedUserId(existingUser.id)
        return Resource.Success(existingUser.toDomain())
    }

    override suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Resource<User> {
        val displayName = name.trim()
        val normalizedEmail = normalizeEmail(email)

        if (displayName.isBlank()) {
            return Resource.Error("Name is required")
        }

        if (normalizedEmail.isBlank()) {
            return Resource.Error("Email is required")
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            return Resource.Error("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }

        if (userDao.getUserByEmail(normalizedEmail) != null) {
            return Resource.Error("An account already exists with this email")
        }

        val now = Instant.now()
        val user = User(
            id = "usr-${UUID.randomUUID()}",
            displayName = displayName,
            email = normalizedEmail,
            phoneNumber = phoneNumber.trim().ifBlank { null },
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
                availabilityNote = null,
                skillLevel = null,
                rating = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )

        sessionLocalDataSource.setAuthenticatedUserId(user.id)
        return Resource.Success(user)
    }

    override suspend fun logout(): Resource<Unit> {
        sessionLocalDataSource.clearAuthenticatedUserId()
        return Resource.Success(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAuthenticatedUser(): Flow<User?> {
        return sessionLocalDataSource.authenticatedUserId
            .flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(null)
                } else {
                    userDao.observeUserById(userId).map { entity -> entity?.toDomain() }
                }
            }
    }

    override suspend fun getAuthenticatedUser(): User? {
        val userId = sessionLocalDataSource.authenticatedUserId.value ?: return null
        return userDao.getUserById(userId)?.toDomain()
    }

    override suspend fun updateCurrentUserProfile(
        displayName: String,
        phoneNumber: String?,
        avatarUrl: String?
    ): Resource<User> {
        val userId = sessionLocalDataSource.authenticatedUserId.value
            ?: return Resource.Error("No authenticated user")

        val existingUser = userDao.getUserById(userId)
            ?: return Resource.Error("Profile not found")

        val updatedDisplayName = displayName.trim()
        if (updatedDisplayName.isBlank()) {
            return Resource.Error("Name cannot be empty")
        }

        val now = Instant.now().toEpochMilli()
        val updated = existingUser.copy(
            displayName = updatedDisplayName,
            phoneNumber = phoneNumber?.trim()?.takeIf { it.isNotBlank() },
            avatarUrl = avatarUrl?.trim()?.takeIf { it.isNotBlank() },
            updatedAt = now
        )

        userDao.upsertUser(updated)
        return Resource.Success(updated.toDomain())
    }

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

    private suspend fun ensureDemoUser(): User {
        val existing = userDao.getUserByEmail(DEMO_USER_EMAIL)
        if (existing != null) {
            return existing.toDomain()
        }

        val now = Instant.now()
        val demoUser = User(
            id = "usr-demo",
            displayName = "Dakti Demo User",
            email = DEMO_USER_EMAIL,
            phoneNumber = "+2348000000000",
            avatarUrl = null,
            role = UserRole.BOTH,
            bio = "Demo account",
            createdAt = now,
            updatedAt = now
        )

        userDao.upsertUser(demoUser.toEntity())
        userDao.upsertPlayer(
            Player(
                userId = demoUser.id,
                preferredSport = "Football",
                availabilityNote = "Weekends",
                skillLevel = "Intermediate",
                rating = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )

        return demoUser
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private companion object {
        private const val DEMO_USER_EMAIL: String = "demo@dakti.app"
        private const val DEMO_USER_PASSWORD: String = "demo123"
        private const val MIN_PASSWORD_LENGTH: Int = 6
    }
}
