package com.dakti.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val message: String = "Use demo actions to test navigation."
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun loginDemo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Signing in...") }
            when (val result = authRepository.login("demo@dakti.app", "demo")) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            message = "Welcome ${result.data.displayName}"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, message = result.message) }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun registerDemo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Creating account...") }
            when (val result = authRepository.register("New Player", "new@dakti.app", "demo")) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            message = "Account ready for ${result.data.displayName}"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, message = result.message) }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState(message = "Logged out")
        }
    }
}
