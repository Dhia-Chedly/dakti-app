package com.dakti.app.data.local.session

import android.content.SharedPreferences
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

    override val authenticatedUserId: StateFlow<String?> = _authenticatedUserId.asStateFlow()

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

    private companion object {
        private const val KEY_AUTHENTICATED_USER_ID: String = "key_authenticated_user_id"
    }
}
