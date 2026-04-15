package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserWithProfiles
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Resource<User>
    suspend fun logout(): Resource<Unit>

    fun observeAuthenticatedUser(): Flow<User?>
    suspend fun getAuthenticatedUser(): User?

    suspend fun updateCurrentUserProfile(
        displayName: String,
        phoneNumber: String?,
        avatarUrl: String?
    ): Resource<User>

    suspend fun getUserById(userId: String): User?
    fun observeUser(userId: String): Flow<User?>
    suspend fun getUserWithProfiles(userId: String): UserWithProfiles?

    suspend fun upsertOrganizerProfile(organizer: Organizer): Resource<Unit>
    suspend fun upsertPlayerProfile(player: Player): Resource<Unit>
}
