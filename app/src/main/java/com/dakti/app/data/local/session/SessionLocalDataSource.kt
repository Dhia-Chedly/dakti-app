package com.dakti.app.data.local.session

import kotlinx.coroutines.flow.StateFlow

interface SessionLocalDataSource {
    val authenticatedUserId: StateFlow<String?>
    val accessToken: StateFlow<String?>
    val refreshToken: StateFlow<String?>
    val onboardingCompleted: StateFlow<Boolean>

    fun setAuthenticatedUserId(userId: String)
    fun clearAuthenticatedUserId()

    fun setSession(
        userId: String,
        accessToken: String,
        refreshToken: String?
    )
    fun clearSession()

    fun setOnboardingCompleted(completed: Boolean)
}
