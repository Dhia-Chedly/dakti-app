package com.dakti.app.ui.screens.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.dakti.app.ui.theme.DaktiTheme
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun splashScreen_rendersBranding() {
        composeRule.setContent {
            DaktiTheme(darkTheme = false) {
                SplashScreen()
            }
        }

        composeRule.onNodeWithText("Dakti").assertIsDisplayed()
        composeRule.onNodeWithText("Your sports planning hub").assertIsDisplayed()
    }

    @Test
    fun splashScreen_rendersBranding_darkTheme() {
        composeRule.setContent {
            DaktiTheme(darkTheme = true) {
                SplashScreen()
            }
        }

        composeRule.onNodeWithText("Dakti").assertIsDisplayed()
        composeRule.onNodeWithText("Your sports planning hub").assertIsDisplayed()
    }
}
