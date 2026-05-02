package com.dakti.app.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.util.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ThemeUiState(
    val mode: AppThemeMode = AppThemeMode.LIGHT
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val sessionLocalDataSource: SessionLocalDataSource
) : ViewModel() {

    val uiState: StateFlow<ThemeUiState> = sessionLocalDataSource.themeMode
        .map { mode -> ThemeUiState(mode = mode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeUiState(mode = sessionLocalDataSource.themeMode.value)
        )

    fun setThemeMode(mode: AppThemeMode) {
        sessionLocalDataSource.setThemeMode(mode)
    }

    companion object {
        fun resolveDarkTheme(
            mode: AppThemeMode,
            isSystemDarkTheme: Boolean
        ): Boolean {
            return when (mode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemDarkTheme
            }
        }
    }
}
