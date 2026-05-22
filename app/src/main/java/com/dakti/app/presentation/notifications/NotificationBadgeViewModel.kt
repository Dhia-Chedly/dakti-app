package com.dakti.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.domain.usecase.ObserveNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationBadgeUiState(
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val observeNotificationsUseCase: ObserveNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationBadgeUiState())
    val uiState: StateFlow<NotificationBadgeUiState> = _uiState.asStateFlow()

    private var notificationJob: Job? = null

    init {
        observeCurrentUserNotifications()
    }

    private fun observeCurrentUserNotifications() {
        viewModelScope.launch {
            authRepository.observeAuthenticatedUser().collect { user ->
                notificationJob?.cancel()
                if (user == null) {
                    _uiState.value = NotificationBadgeUiState(unreadCount = 0)
                } else {
                    notificationJob = launch {
                        observeNotificationsUseCase(user.id).collect { notifications ->
                            _uiState.update { state ->
                                state.copy(
                                    unreadCount = notifications.count { notification ->
                                        !notification.isRead
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
