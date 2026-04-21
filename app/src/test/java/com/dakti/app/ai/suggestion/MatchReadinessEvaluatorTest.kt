package com.dakti.app.ai.suggestion

import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.testutil.TestData
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchReadinessEvaluatorTest {

    private val evaluator = MatchReadinessEvaluator()

    @Test
    fun evaluate_whenConfirmedMeetsRequired_returnsReady() {
        val match = TestData.matchWithContext(
            requiredPlayers = 8,
            confirmedPlayers = 8,
            pendingPlayers = 0,
            declinedPlayers = 0
        )

        val result = evaluator.evaluate(match, now = TestData.now)

        assertEquals(MatchReadinessStatus.READY, result.status)
        assertFalse(result.shouldAlertOrganizer)
        assertEquals(0, result.missingPlayers)
    }

    @Test
    fun evaluate_whenCloseToKickoffAndMissingPlayers_returnsInsufficientPlayers() {
        val base = TestData.matchWithContext(
            requiredPlayers = 10,
            confirmedPlayers = 5,
            pendingPlayers = 1,
            declinedPlayers = 0
        )
        val match = base.copy(
            match = base.match.copy(scheduledStartTime = TestData.now.plus(Duration.ofHours(2)))
        )

        val result = evaluator.evaluate(match, now = TestData.now)

        assertEquals(MatchReadinessStatus.INSUFFICIENT_PLAYERS, result.status)
        assertTrue(result.shouldAlertOrganizer)
        assertEquals(5, result.missingPlayers)
    }

    @Test
    fun evaluate_whenUrgentWithHighPending_returnsAtRisk() {
        val base = TestData.matchWithContext(
            requiredPlayers = 10,
            confirmedPlayers = 7,
            pendingPlayers = 3,
            declinedPlayers = 0
        )
        val match = base.copy(
            match = base.match.copy(scheduledStartTime = TestData.now.plus(Duration.ofHours(6)))
        )

        val result = evaluator.evaluate(match, now = TestData.now)

        assertEquals(MatchReadinessStatus.AT_RISK, result.status)
        assertTrue(result.shouldAlertOrganizer)
    }
}
