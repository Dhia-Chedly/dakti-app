package com.dakti.app.data.local.session

import com.dakti.app.util.AppThemeMode
import kotlinx.coroutines.flow.StateFlow

interface SessionLocalDataSource {
    val authenticatedUserId: StateFlow<String?>
    val accessToken: StateFlow<String?>
    val refreshToken: StateFlow<String?>
    val onboardingCompleted: StateFlow<Boolean>
    val themeMode: StateFlow<AppThemeMode>

    fun setAuthenticatedUserId(userId: String)
    fun clearAuthenticatedUserId()

    fun setSession(
        userId: String,
        accessToken: String,
        refreshToken: String?
    )
    fun clearSession()

    fun setOnboardingCompleted(completed: Boolean)
    fun setThemeMode(mode: AppThemeMode)
}
