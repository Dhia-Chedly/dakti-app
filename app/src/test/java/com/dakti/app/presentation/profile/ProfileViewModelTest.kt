package com.dakti.app.presentation.profile

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.testutil.FakeAuthRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.AppThemeMode
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val sessionLocalDataSource = FakeSessionLocalDataSource()

    @Test
    fun onThemeModeSelected_updatesStateAndPersists() = runTest {
        val viewModel = createAuthenticatedViewModel()

        viewModel.onThemeModeSelected(AppThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.themeMode)
        assertEquals(AppThemeMode.DARK, sessionLocalDataSource.themeMode.value)
    }

    @Test
    fun saveProfile_stillWorksAfterThemeChange() = runTest {
        val user = TestData.user()
        authRepository.updateProfileResult = Resource.Success(user)
        val viewModel = createAuthenticatedViewModel()

        viewModel.startEditing()
        viewModel.onDisplayNameChanged("Updated User")
        viewModel.onThemeModeSelected(AppThemeMode.SYSTEM)
        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppThemeMode.SYSTEM, state.themeMode)
        assertEquals("Profile updated", state.statusMessage)
        assertFalse(state.isEditing)
    }

    private suspend fun TestScope.createAuthenticatedViewModel(): ProfileViewModel {
        val user = TestData.user()
        authRepository.loginResult = Resource.Success(user)
        authRepository.login(user.email, "demo123")

        val viewModel = ProfileViewModel(
            authRepository = authRepository,
            sessionLocalDataSource = sessionLocalDataSource
        )
        advanceUntilIdle()
        return viewModel
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
