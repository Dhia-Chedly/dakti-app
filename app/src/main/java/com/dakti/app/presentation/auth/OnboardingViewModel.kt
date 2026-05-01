package com.dakti.app.presentation.auth

import androidx.lifecycle.ViewModel
import com.dakti.app.data.local.session.SessionLocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class OnboardingStageKind {
    VENUES,
    MATCHES,
    ASSISTANT,
    READY
}

data class OnboardingStage(
    val kind: OnboardingStageKind,
    val title: String,
    val subtitle: String,
    val primaryEmote: String,
    val secondaryEmote: String
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionLocalDataSource: SessionLocalDataSource
) : ViewModel() {

    val stages: List<OnboardingStage> = listOf(
        OnboardingStage(
            kind = OnboardingStageKind.VENUES,
            title = "Find Venues Fast",
            subtitle = "Discover nearby stadiums and courts with one tap and keep your schedule moving.",
            primaryEmote = "⚽",
            secondaryEmote = "🏟️"
        ),
        OnboardingStage(
            kind = OnboardingStageKind.MATCHES,
            title = "Create & Fill Matches",
            subtitle = "Set player targets, track readiness, and fill open spots before kickoff.",
            primaryEmote = "🏀",
            secondaryEmote = "👥"
        ),
        OnboardingStage(
            kind = OnboardingStageKind.ASSISTANT,
            title = "Coordinate with AI",
            subtitle = "Generate invites, reminders, and quick planning ideas for every game night.",
            primaryEmote = "🎾",
            secondaryEmote = "🤖"
        ),
        OnboardingStage(
            kind = OnboardingStageKind.READY,
            title = "Ready to Play",
            subtitle = "Bring your team together and run each match day with confidence.",
            primaryEmote = "🏐",
            secondaryEmote = "🔥"
        )
    )

    fun completeOnboarding() {
        sessionLocalDataSource.setOnboardingCompleted(true)
    }
}

