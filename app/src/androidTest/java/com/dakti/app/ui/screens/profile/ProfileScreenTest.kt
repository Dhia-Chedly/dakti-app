package com.dakti.app.ui.screens.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dakti.app.presentation.profile.ProfileUiState
import com.dakti.app.ui.theme.DaktiTheme
import com.dakti.app.util.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun profileScreen_showsAppearanceModesAndHandlesSelection() {
        var selectedMode: AppThemeMode? = null

        composeRule.setContent {
            DaktiTheme {
                ProfileScreen(
                    uiState = sampleState(),
                    onDisplayNameChanged = {},
                    onPhoneNumberChanged = {},
                    onAvatarUrlChanged = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onSaveProfile = {},
                    onThemeModeSelected = { mode -> selectedMode = mode },
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithText("System").assertIsDisplayed()

        composeRule.onNodeWithText("Dark").performClick()
        assertEquals(AppThemeMode.DARK, selectedMode)
    }

    private fun sampleState(): ProfileUiState = ProfileUiState(
        isLoading = false,
        userId = "user-1",
        displayName = "Demo User",
        email = "demo@dakti.app",
        phoneNumber = "+2348012345678",
        avatarUrl = "",
        roleLabel = "Organizer",
        isEditing = false,
        isSaving = false,
        isLoggedOut = false,
        themeMode = AppThemeMode.LIGHT
    )
}
