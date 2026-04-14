package com.dakti.app.data.repository

import com.dakti.app.domain.model.AppUser
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    override suspend fun login(email: String, password: String): Resource<AppUser> {
        return Resource.Success(
            AppUser(
                id = "demo-user",
                displayName = "Dakti Player",
                email = email
            )
        )
    }

    override suspend fun register(name: String, email: String, password: String): Resource<AppUser> {
        return Resource.Success(
            AppUser(
                id = "registered-user",
                displayName = name,
                email = email
            )
        )
    }

    override suspend fun logout(): Resource<Unit> = Resource.Success(Unit)
}
