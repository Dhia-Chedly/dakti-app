package com.dakti.app.presentation.theme

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.util.AppThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun setThemeMode_updatesSessionAndUiState() = runTest {
        val sessionLocalDataSource = FakeSessionLocalDataSource()
        val viewModel = ThemeViewModel(sessionLocalDataSource)
        advanceUntilIdle()

        assertEquals(AppThemeMode.LIGHT, viewModel.uiState.value.mode)

        viewModel.setThemeMode(AppThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(AppThemeMode.DARK, sessionLocalDataSource.themeMode.value)
        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.mode)
    }

    @Test
    fun resolveDarkTheme_respectsAllModes() {
        assertFalse(ThemeViewModel.resolveDarkTheme(AppThemeMode.LIGHT, isSystemDarkTheme = true))
        assertTrue(ThemeViewModel.resolveDarkTheme(AppThemeMode.DARK, isSystemDarkTheme = false))
        assertTrue(ThemeViewModel.resolveDarkTheme(AppThemeMode.SYSTEM, isSystemDarkTheme = true))
        assertFalse(ThemeViewModel.resolveDarkTheme(AppThemeMode.SYSTEM, isSystemDarkTheme = false))
    }
}

private class FakeSessionLocalDataSource : SessionLocalDataSource {
    private val _authenticatedUserId = MutableStateFlow<String?>(null)
    private val _accessToken = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)
    private val _onboardingCompleted = MutableStateFlow(false)
    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)

    override val authenticatedUserId: StateFlow<String?> = _authenticatedUserId
    override val accessToken: StateFlow<String?> = _accessToken
    override val refreshToken: StateFlow<String?> = _refreshToken
    override val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted
    override val themeMode: StateFlow<AppThemeMode> = _themeMode

    override fun setAuthenticatedUserId(userId: String) {
        _authenticatedUserId.value = userId
    }

    override fun clearAuthenticatedUserId() {
        _authenticatedUserId.value = null
    }

    override fun setSession(userId: String, accessToken: String, refreshToken: String?) {
        _authenticatedUserId.value = userId
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
    }

    override fun clearSession() {
        _authenticatedUserId.value = null
        _accessToken.value = null
        _refreshToken.value = null
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }

    override fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }
}
