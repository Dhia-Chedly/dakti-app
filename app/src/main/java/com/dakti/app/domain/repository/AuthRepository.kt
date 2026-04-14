package com.dakti.app.domain.repository

import com.dakti.app.domain.model.AppUser
import com.dakti.app.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<AppUser>
    suspend fun register(name: String, email: String, password: String): Resource<AppUser>
    suspend fun logout(): Resource<Unit>
}
