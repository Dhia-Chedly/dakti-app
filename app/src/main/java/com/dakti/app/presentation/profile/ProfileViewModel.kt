package com.dakti.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.domain.model.User
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.util.AppThemeMode
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userId: String? = null,
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String = "",
    val roleLabel: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionLocalDataSource: SessionLocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        observeCurrentUser()
        observeThemeMode()
    }

    fun startEditing() {
        currentUser?.let { user ->
            _uiState.update {
                it.copy(
                    isEditing = true,
                    displayName = user.displayName,
                    phoneNumber = user.phoneNumber.orEmpty(),
                    avatarUrl = user.avatarUrl.orEmpty(),
                    errorMessage = null,
                    statusMessage = null
                )
            }
        }
    }

    fun cancelEditing() {
        currentUser?.let { user ->
            _uiState.update {
                it.copy(
                    isEditing = false,
                    displayName = user.displayName,
                    phoneNumber = user.phoneNumber.orEmpty(),
                    avatarUrl = user.avatarUrl.orEmpty(),
                    errorMessage = null
                )
            }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update {
            it.copy(
                displayName = value,
                errorMessage = null,
                statusMessage = null
            )
        }
    }

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value,
                errorMessage = null,
                statusMessage = null
            )
        }
    }

    fun onAvatarUrlChanged(value: String) {
        _uiState.update {
            it.copy(
                avatarUrl = value,
                errorMessage = null,
                statusMessage = null
            )
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = null) }
            when (
                val result = authRepository.updateCurrentUserProfile(
                    displayName = state.displayName,
                    phoneNumber = state.phoneNumber,
                    avatarUrl = state.avatarUrl
                )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            statusMessage = "Profile updated"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isSaving = true) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(isLoggedOut = true, statusMessage = "Logged out")
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun onThemeModeSelected(mode: AppThemeMode) {
        sessionLocalDataSource.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun onLogoutHandled() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.observeAuthenticatedUser().collect { user ->
                currentUser = user
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = null,
                            errorMessage = null,
                            isLoggedOut = true
                        )
                    }
                    return@collect
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        userId = user.id,
                        displayName = if (state.isEditing) state.displayName else user.displayName,
                        email = user.email,
                        phoneNumber = if (state.isEditing) state.phoneNumber else user.phoneNumber.orEmpty(),
                        avatarUrl = if (state.isEditing) state.avatarUrl else user.avatarUrl.orEmpty(),
                        roleLabel = user.role.name.lowercase().replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase() else char.toString()
                        },
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            sessionLocalDataSource.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }
}
