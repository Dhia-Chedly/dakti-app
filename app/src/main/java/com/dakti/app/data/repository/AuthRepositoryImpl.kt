package com.dakti.app.data.repository

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import com.dakti.app.data.remote.supabase.model.ProfileRowDto
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.model.UserWithProfiles
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.util.Resource
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val currentUser = MutableStateFlow<User?>(null)

    init {
        scope.launch {
            restoreAuthenticatedUser()
        }
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        return runCatching {
            val session = supabaseRemoteDataSource.signIn(email = email.trim(), password = password)
            sessionLocalDataSource.setSession(
                userId = session.user.id,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )

            val profile = ensureProfileExists(
                userId = session.user.id,
                email = session.user.email ?: email.trim(),
                fullName = (session.user.userMetadata?.get("full_name") as? String).orEmpty(),
                phone = session.user.userMetadata?.get("phone") as? String
            )

            val domainUser = profile.toDomainUser()
            currentUser.value = domainUser
            Resource.Success(domainUser)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Login failed")
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Resource<User> {
        return runCatching {
            val signup = supabaseRemoteDataSource.signUp(
                email = email.trim(),
                password = password,
                metadata = mapOf(
                    "full_name" to name.trim(),
                    "phone" to phoneNumber.trim()
                )
            )

            val session = signup.session
                ?: throw IllegalStateException("Sign up completed. Please verify your email, then login.")

            val authUser = signup.user ?: session.user
            sessionLocalDataSource.setSession(
                userId = authUser.id,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )

            val profile = ensureProfileExists(
                userId = authUser.id,
                email = authUser.email ?: email.trim(),
                fullName = name,
                phone = phoneNumber
            )

            val domainUser = profile.toDomainUser()
            currentUser.value = domainUser
            Resource.Success(domainUser)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Registration failed")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        sessionLocalDataSource.clearSession()
        currentUser.value = null
        return Resource.Success(Unit)
    }

    override fun observeAuthenticatedUser(): Flow<User?> = currentUser.asStateFlow()

    override suspend fun getAuthenticatedUser(): User? {
        currentUser.value?.let { return it }
        restoreAuthenticatedUser()
        return currentUser.value
    }

    override suspend fun updateCurrentUserProfile(
        displayName: String,
        phoneNumber: String?,
        avatarUrl: String?
    ): Resource<User> {
        val userId = sessionLocalDataSource.authenticatedUserId.value
            ?: return Resource.Error("No authenticated user")

        return runCatching {
            val profile = supabaseRemoteDataSource.updateProfile(
                userId = userId,
                payload = mapOf(
                    "full_name" to displayName.trim(),
                    "phone" to phoneNumber?.trim()?.ifBlank { null },
                    "avatar_url" to avatarUrl?.trim()?.ifBlank { null },
                    "updated_at" to Instant.now().toString()
                )
            ) ?: throw IllegalStateException("Failed to update profile")

            val user = profile.toDomainUser()
            currentUser.value = user
            Resource.Success(user)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not update profile")
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return runCatching {
            supabaseRemoteDataSource.getProfile(userId)?.toDomainUser()
        }.getOrNull()
    }

    override fun observeUser(userId: String): Flow<User?> {
        return currentUser.map { user ->
            if (user?.id == userId) user else null
        }
    }

    override suspend fun getUserWithProfiles(userId: String): UserWithProfiles? {
        val profile = supabaseRemoteDataSource.getProfile(userId) ?: return null
        val user = profile.toDomainUser()
        return UserWithProfiles(
            user = user,
            organizer = if (profile.role.equals("organizer", ignoreCase = true)) {
                Organizer(
                    userId = profile.id,
                    rating = 0.0,
                    totalHostedMatches = 0,
                    organizationName = null,
                    isVerified = false,
                    createdAt = profile.createdAt.toInstantOrNow(),
                    updatedAt = profile.updatedAt.toInstantOrNow()
                )
            } else {
                null
            },
            player = if (profile.role.equals("player", ignoreCase = true)) {
                Player(
                    userId = profile.id,
                    preferredSport = profile.preferredSport.orEmpty(),
                    availabilityNote = profile.availabilityNote,
                    skillLevel = null,
                    rating = null,
                    createdAt = profile.createdAt.toInstantOrNow(),
                    updatedAt = profile.updatedAt.toInstantOrNow()
                )
            } else {
                null
            }
        )
    }

    override suspend fun upsertOrganizerProfile(organizer: Organizer): Resource<Unit> {
        return runCatching {
            val authenticated = currentUser.value
                ?: throw IllegalStateException("No authenticated user")
            supabaseRemoteDataSource.upsertProfile(
                payload = mapOf(
                    "id" to organizer.userId,
                    "role" to "organizer",
                    "full_name" to authenticated.displayName,
                    "email" to authenticated.email,
                    "updated_at" to Instant.now().toString()
                )
            )
            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not save organizer profile")
        }
    }

    override suspend fun upsertPlayerProfile(player: Player): Resource<Unit> {
        return runCatching {
            val authenticated = currentUser.value
                ?: throw IllegalStateException("No authenticated user")
            supabaseRemoteDataSource.upsertProfile(
                payload = mapOf(
                    "id" to player.userId,
                    "role" to "player",
                    "full_name" to authenticated.displayName,
                    "email" to authenticated.email,
                    "preferred_sport" to player.preferredSport,
                    "availability_note" to player.availabilityNote,
                    "updated_at" to Instant.now().toString()
                )
            )
            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not save player profile")
        }
    }

    private suspend fun restoreAuthenticatedUser() {
        val userId = sessionLocalDataSource.authenticatedUserId.value ?: return
        val profile = runCatching { supabaseRemoteDataSource.getProfile(userId) }.getOrNull() ?: return
        currentUser.value = profile.toDomainUser()
    }

    private suspend fun ensureProfileExists(
        userId: String,
        email: String,
        fullName: String,
        phone: String?
    ): ProfileRowDto {
        val existing = supabaseRemoteDataSource.getProfile(userId)
        if (existing != null) {
            return existing
        }

        val created = supabaseRemoteDataSource.upsertProfile(
            payload = mapOf(
                "id" to userId,
                "email" to email,
                "full_name" to fullName.ifBlank { email.substringBefore("@") },
                "phone" to phone,
                "role" to "organizer"
            )
        )

        return created ?: throw IllegalStateException("Unable to create user profile")
    }

    private fun ProfileRowDto.toDomainUser(): User {
        val role = if (role.equals("organizer", ignoreCase = true)) {
            UserRole.ORGANIZER
        } else {
            UserRole.PLAYER
        }
        return User(
            id = id,
            displayName = fullName,
            email = email,
            phoneNumber = phone,
            avatarUrl = avatarUrl,
            role = role,
            bio = availabilityNote,
            createdAt = createdAt.toInstantOrNow(),
            updatedAt = updatedAt.toInstantOrNow()
        )
    }

    private fun String.toInstantOrNow(): Instant =
        runCatching { Instant.parse(this) }.getOrElse { Instant.now() }
}
