package com.dakti.app.ui.screens.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.dakti.app.presentation.auth.OnboardingStage
import com.dakti.app.presentation.auth.OnboardingStageKind
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onboarding_nextAndSwipe_movesBetweenStages() {
        composeRule.setContent {
            OnboardingScreen(
                stages = sampleStages(),
                onSkip = {},
                onComplete = {}
            )
        }

        composeRule.onNodeWithText("Find Venues Fast").assertIsDisplayed()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Create & Fill Matches").assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("Coordinate with AI").assertIsDisplayed()
    }

    @Test
    fun onboarding_skipAndGetStarted_triggerCallbacks() {
        var skipped = false
        var completed = false

        composeRule.setContent {
            OnboardingScreen(
                stages = sampleStages(),
                onSkip = { skipped = true },
                onComplete = { completed = true }
            )
        }

        composeRule.onNodeWithText("Skip").performClick()
        assertTrue(skipped)

        composeRule.setContent {
            OnboardingScreen(
                stages = sampleStages(),
                onSkip = {},
                onComplete = { completed = true }
            )
        }

        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Get Started").performClick()
        assertTrue(completed)
    }

    private fun sampleStages(): List<OnboardingStage> = listOf(
        OnboardingStage(
            kind = OnboardingStageKind.VENUES,
            title = "Find Venues Fast",
            subtitle = "Discover nearby stadiums and courts.",
            primaryEmote = "?",
            secondaryEmote = "???"
        ),
        OnboardingStage(
            kind = OnboardingStageKind.MATCHES,
            title = "Create & Fill Matches",
            subtitle = "Track readiness and fill open slots.",
            primaryEmote = "??",
            secondaryEmote = "??"
        ),
        OnboardingStage(
            kind = OnboardingStageKind.ASSISTANT,
            title = "Coordinate with AI",
            subtitle = "Generate reminders and invites.",
            primaryEmote = "??",
            secondaryEmote = "??"
        ),
        OnboardingStage(
            kind = OnboardingStageKind.READY,
            title = "Ready to Play",
            subtitle = "Start managing your match day.",
            primaryEmote = "??",
            secondaryEmote = "??"
        )
    )
}
