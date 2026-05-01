package com.dakti.app.presentation.auth

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.testutil.FakeAuthRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchCoordinatorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val sessionLocalDataSource = FakeSessionLocalDataSource()

    @Test
    fun resolveLaunchDestination_authenticatedUser_goesToMainGraph() = runTest {
        val user = TestData.user()
        authRepository.loginResult = Resource.Success(user)
        authRepository.login(user.email, "demo123")

        val viewModel = LaunchCoordinatorViewModel(
            authRepository = authRepository,
            sessionLocalDataSource = sessionLocalDataSource
        )

        advanceTimeBy(1200)
        advanceUntilIdle()

        assertEquals(LaunchDestination.MainGraph, viewModel.uiState.value.destination)
    }

    @Test
    fun resolveLaunchDestination_firstLaunch_goesToOnboarding() = runTest {
        sessionLocalDataSource.setOnboardingCompleted(false)

        val viewModel = LaunchCoordinatorViewModel(
            authRepository = authRepository,
            sessionLocalDataSource = sessionLocalDataSource
        )

        advanceTimeBy(1200)
        advanceUntilIdle()

        assertEquals(LaunchDestination.Onboarding, viewModel.uiState.value.destination)
    }

    @Test
    fun resolveLaunchDestination_onboardingCompleted_goesToWelcome() = runTest {
        sessionLocalDataSource.setOnboardingCompleted(true)

        val viewModel = LaunchCoordinatorViewModel(
            authRepository = authRepository,
            sessionLocalDataSource = sessionLocalDataSource
        )

        advanceTimeBy(1200)
        advanceUntilIdle()

        assertEquals(LaunchDestination.Welcome, viewModel.uiState.value.destination)
    }

    @Test
    fun onboardingViewModel_completeOnboarding_updatesPersistence() {
        val viewModel = OnboardingViewModel(sessionLocalDataSource)

        viewModel.completeOnboarding()

        assertTrue(sessionLocalDataSource.onboardingCompleted.value)
    }
}

private class FakeSessionLocalDataSource : SessionLocalDataSource {
    private val _authenticatedUserId = MutableStateFlow<String?>(null)
    private val _accessToken = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)
    private val _onboardingCompleted = MutableStateFlow(false)

    override val authenticatedUserId: StateFlow<String?> = _authenticatedUserId
    override val accessToken: StateFlow<String?> = _accessToken
    override val refreshToken: StateFlow<String?> = _refreshToken
    override val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

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
}
