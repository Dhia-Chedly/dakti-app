package com.dakti.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface LaunchDestination {
    data object MainGraph : LaunchDestination
    data object Onboarding : LaunchDestination
    data object Welcome : LaunchDestination
}

data class LaunchCoordinatorUiState(
    val isResolving: Boolean = true,
    val destination: LaunchDestination? = null
)

@HiltViewModel
class LaunchCoordinatorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionLocalDataSource: SessionLocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchCoordinatorUiState())
    val uiState: StateFlow<LaunchCoordinatorUiState> = _uiState.asStateFlow()

    init {
        resolveLaunchDestination()
    }

    private fun resolveLaunchDestination() {
        viewModelScope.launch {
            val splashDelay = async {
                delay(MIN_SPLASH_DURATION_MS)
            }
            val userDeferred = async {
                authRepository.getAuthenticatedUser()
            }

            splashDelay.await()
            val user = userDeferred.await()
            val onboardingCompleted = sessionLocalDataSource.onboardingCompleted.value

            _uiState.update {
                it.copy(
                    isResolving = false,
                    destination = when {
                        user != null -> LaunchDestination.MainGraph
                        onboardingCompleted -> LaunchDestination.Welcome
                        else -> LaunchDestination.Onboarding
                    }
                )
            }
        }
    }

    companion object {
        private const val MIN_SPLASH_DURATION_MS: Long = 1100L
    }
}

