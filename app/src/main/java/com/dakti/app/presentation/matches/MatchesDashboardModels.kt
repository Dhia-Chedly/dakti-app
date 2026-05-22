package com.dakti.app.presentation.matches

import com.dakti.app.domain.model.InvitationResponseStatus

enum class MatchesDashboardTab {
    UPCOMING,
    INVITES,
    PAST
}

enum class MatchesDashboardStatusTone {
    POSITIVE,
    WARNING,
    DANGER,
    INFO,
    MUTED
}

data class MatchDashboardCardUi(
    val id: String,
    val title: String,
    val sportType: String,
    val venueName: String,
    val scheduledLabel: String,
    val statusLabel: String,
    val statusTone: MatchesDashboardStatusTone,
    val requiredPlayers: Int,
    val confirmedPlayersCount: Int,
    val pendingPlayersCount: Int,
    val remainingSpots: Int,
    val needsAttention: Boolean,
    val actionLabel: String
)

data class InviteDashboardCardUi(
    val invitationId: String,
    val matchId: String,
    val title: String,
    val sportType: String,
    val venueName: String,
    val scheduledLabel: String,
    val status: InvitationResponseStatus,
    val statusLabel: String,
    val canRespond: Boolean,
    val isResponding: Boolean
)
