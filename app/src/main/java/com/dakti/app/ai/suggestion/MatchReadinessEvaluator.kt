package com.dakti.app.ai.suggestion

import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

data class MatchReadinessAssessment(
    val status: MatchReadinessStatus,
    val reason: String,
    val shouldAlertOrganizer: Boolean,
    val missingPlayers: Int,
    val minutesUntilMatch: Long
)

class MatchReadinessEvaluator @Inject constructor() {

    fun evaluate(
        matchWithContext: MatchWithContext,
        now: Instant = Instant.now()
    ): MatchReadinessAssessment {
        val match = matchWithContext.match
        val required = match.requiredPlayers.coerceAtLeast(1)
        val confirmed = matchWithContext.confirmedPlayersCount.coerceAtLeast(0)
        val pending = matchWithContext.pendingPlayersCount.coerceAtLeast(0)
        val missing = (required - confirmed).coerceAtLeast(0)
        val minutesUntilMatch = Duration.between(now, match.scheduledStartTime).toMinutes()

        if (match.status == MatchStatus.CANCELLED || match.status == MatchStatus.COMPLETED) {
            return MatchReadinessAssessment(
                status = MatchReadinessStatus.READY,
                reason = "Match is not active for monitoring.",
                shouldAlertOrganizer = false,
                missingPlayers = missing,
                minutesUntilMatch = minutesUntilMatch
            )
        }

        if (confirmed >= required) {
            return MatchReadinessAssessment(
                status = MatchReadinessStatus.READY,
                reason = "Confirmed players already meet the required count.",
                shouldAlertOrganizer = false,
                missingPlayers = 0,
                minutesUntilMatch = minutesUntilMatch
            )
        }

        if (minutesUntilMatch <= CRITICAL_WINDOW_MINUTES) {
            return MatchReadinessAssessment(
                status = MatchReadinessStatus.INSUFFICIENT_PLAYERS,
                reason = "Kickoff is close and there are still $missing open spots.",
                shouldAlertOrganizer = true,
                missingPlayers = missing,
                minutesUntilMatch = minutesUntilMatch
            )
        }

        if (confirmed + pending < required) {
            return MatchReadinessAssessment(
                status = MatchReadinessStatus.INSUFFICIENT_PLAYERS,
                reason = "Even if all pending invites accept, players are still below target.",
                shouldAlertOrganizer = minutesUntilMatch <= URGENT_WINDOW_MINUTES,
                missingPlayers = missing,
                minutesUntilMatch = minutesUntilMatch
            )
        }

        if (pending >= HIGH_PENDING_THRESHOLD && minutesUntilMatch <= URGENT_WINDOW_MINUTES) {
            return MatchReadinessAssessment(
                status = MatchReadinessStatus.AT_RISK,
                reason = "$pending invitation responses are still pending near kickoff.",
                shouldAlertOrganizer = true,
                missingPlayers = missing,
                minutesUntilMatch = minutesUntilMatch
            )
        }

        return MatchReadinessAssessment(
            status = MatchReadinessStatus.NEEDS_ORGANIZER_ACTION,
            reason = "Current confirmations are below required players.",
            shouldAlertOrganizer = minutesUntilMatch <= PRE_ALERT_WINDOW_MINUTES,
            missingPlayers = missing,
            minutesUntilMatch = minutesUntilMatch
        )
    }

    private companion object {
        private const val PRE_ALERT_WINDOW_MINUTES: Long = 24 * 60
        private const val URGENT_WINDOW_MINUTES: Long = 12 * 60
        private const val CRITICAL_WINDOW_MINUTES: Long = 3 * 60
        private const val HIGH_PENDING_THRESHOLD: Int = 2
    }
}

