package com.dakti.app.data.local.session

import android.content.SharedPreferences
import com.dakti.app.util.AppThemeMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SharedPreferencesSessionLocalDataSource @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SessionLocalDataSource {

    private val _authenticatedUserId = MutableStateFlow(
        sharedPreferences.getString(KEY_AUTHENTICATED_USER_ID, null)
    )
    private val _accessToken = MutableStateFlow(
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    )
    private val _refreshToken = MutableStateFlow(
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    )
    private val _onboardingCompleted = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    private val _themeMode = MutableStateFlow(
        AppThemeMode.fromStorage(sharedPreferences.getString(KEY_THEME_MODE, null))
    )

    override val authenticatedUserId: StateFlow<String?> = _authenticatedUserId.asStateFlow()
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    override val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()
    override val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()
    override val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    override fun setAuthenticatedUserId(userId: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTHENTICATED_USER_ID, userId)
            .apply()
        _authenticatedUserId.value = userId
    }

    override fun clearAuthenticatedUserId() {
        sharedPreferences.edit()
            .remove(KEY_AUTHENTICATED_USER_ID)
            .apply()
        _authenticatedUserId.value = null
    }

    override fun setSession(
        userId: String,
        accessToken: String,
        refreshToken: String?
    ) {
        sharedPreferences.edit()
            .putString(KEY_AUTHENTICATED_USER_ID, userId)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        _authenticatedUserId.value = userId
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
    }

    override fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_AUTHENTICATED_USER_ID)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        _authenticatedUserId.value = null
        _accessToken.value = null
        _refreshToken.value = null
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .apply()
        _onboardingCompleted.value = completed
    }

    override fun setThemeMode(mode: AppThemeMode) {
        sharedPreferences.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
        _themeMode.value = mode
    }

    private companion object {
        private const val KEY_AUTHENTICATED_USER_ID: String = "key_authenticated_user_id"
        private const val KEY_ACCESS_TOKEN: String = "key_supabase_access_token"
        private const val KEY_REFRESH_TOKEN: String = "key_supabase_refresh_token"
        private const val KEY_ONBOARDING_COMPLETED: String = "key_onboarding_completed"
        private const val KEY_THEME_MODE: String = "key_theme_mode"
    }
}
