package com.dakti.app.domain.model

import java.time.Instant

enum class UserRole {
    PLAYER,
    ORGANIZER,
    BOTH,
    ADMIN
}

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

enum class MatchStatus {
    ORGANIZING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    DRAFT,
    OPEN,
    FULL
}

enum class InvitationResponseStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED
}

enum class NotificationType {
    GENERAL,
    RESERVATION_UPDATE,
    MATCH_UPDATE,
    INVITATION_RECEIVED,
    INVITATION_RESPONSE,
    SYSTEM_ALERT
}

enum class AISuggestionType {
    MATCH_FORMAT,
    PLAYER_ALLOCATION,
    SCHEDULE,
    VENUE_RECOMMENDATION,
    INVITATION_MESSAGE,
    REMINDER_MESSAGE,
    RESCHEDULE_PLAN,
    GENERAL
}

enum class AssistantMessageRole {
    USER,
    ASSISTANT
}

enum class AssistantIntent {
    ORGANIZE_MATCH,
    SUGGEST_VENUE,
    SUGGEST_ALTERNATIVE_SLOT,
    GENERATE_INVITATION_MESSAGE,
    GENERATE_REMINDER_MESSAGE,
    RESCHEDULE_HELP,
    GENERAL_CHAT
}

enum class AssistantGeneratedMessageKind {
    INVITATION,
    REMINDER
}

enum class AssistantActionType {
    NONE,
    CREATE_RESERVATION_ONLY,
    CREATE_MATCH_FROM_RESERVATION,
    CREATE_RESERVATION_AND_MATCH
}

enum class MatchReadinessStatus {
    READY,
    AT_RISK,
    INSUFFICIENT_PLAYERS,
    NEEDS_ORGANIZER_ACTION
}

enum class MonitoringSuggestedActionType {
    REMIND_PENDING_PLAYERS,
    INVITE_MORE_PLAYERS,
    REVIEW_RESCHEDULE_OPTIONS,
    PREPARE_UPDATE_MESSAGE,
    OPEN_ASSISTANT
}

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: UserRole,
    val bio: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Organizer(
    val userId: String,
    val rating: Double,
    val totalHostedMatches: Int,
    val organizationName: String?,
    val isVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Player(
    val userId: String,
    val preferredSport: String,
    val availabilityNote: String?,
    val skillLevel: String?,
    val rating: Double?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Venue(
    val id: String,
    val name: String,
    val sportType: String,
    val description: String?,
    val address: String,
    val contactPhone: String?,
    val imageUrl: String?,
    val city: String,
    val state: String?,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    val pricePerHour: Double,
    val currency: String,
    val amenities: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class TimeSlot(
    val id: String,
    val venueId: String,
    val startTime: Instant,
    val endTime: Instant,
    val isAvailable: Boolean,
    val capacity: Int?
)

data class Reservation(
    val id: String,
    val organizerId: String,
    val venueId: String,
    val timeSlotId: String,
    val venueName: String,
    val timeSlot: String,
    val status: ReservationStatus,
    val totalPrice: Double?,
    val currency: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ReservationDraft(
    val organizerId: String,
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val venueSportType: String,
    val timeSlotId: String,
    val timeSlotLabel: String,
    val totalPrice: Double?,
    val currency: String?,
    val isSlotAvailable: Boolean
)

data class Match(
    val id: String,
    val organizerId: String,
    val venueId: String,
    val reservationId: String?,
    val title: String,
    val sportType: String,
    val scheduledStartTime: Instant,
    val requiredPlayers: Int,
    val status: MatchStatus,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class MatchCreatePayload(
    val sportType: String,
    val scheduledStartTime: Instant,
    val requiredPlayers: Int,
    val description: String?,
    val venueId: String,
    val reservationId: String?
)

data class MatchReservationContext(
    val reservationId: String,
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val sportType: String,
    val scheduledStartTime: Instant,
    val timeSlotLabel: String
)

data class MatchWithContext(
    val match: Match,
    val venueName: String,
    val venueAddress: String,
    val reservationReference: String?,
    val organizerName: String?,
    val invitedPlayersCount: Int,
    val confirmedPlayersCount: Int,
    val pendingPlayersCount: Int,
    val declinedPlayersCount: Int
) {
    val remainingSpots: Int = (match.requiredPlayers - confirmedPlayersCount).coerceAtLeast(0)
}

data class Invitation(
    val id: String,
    val matchId: String,
    val playerId: String,
    val invitedByOrganizerId: String?,
    val matchTitle: String,
    val fromUser: String,
    val status: InvitationResponseStatus,
    val message: String?,
    val sentAt: Instant,
    val respondedAt: Instant?
)

data class InvitePlayerCandidate(
    val playerId: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String?,
    val preferredSport: String,
    val availabilityNote: String?,
    val skillLevel: String?,
    val invitationStatus: InvitationResponseStatus?
) {
    val isAlreadyInvited: Boolean = invitationStatus != null
}

data class InvitationWithContext(
    val invitationId: String,
    val matchId: String,
    val playerId: String,
    val playerName: String,
    val organizerId: String?,
    val organizerName: String,
    val matchTitle: String,
    val sportType: String,
    val venueName: String,
    val venueAddress: String,
    val scheduledStartTime: Instant,
    val requiredPlayers: Int,
    val status: InvitationResponseStatus,
    val message: String?,
    val sentAt: Instant,
    val respondedAt: Instant?
)

data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val isRead: Boolean,
    val relatedMatchId: String?,
    val relatedReservationId: String?,
    val createdAt: Instant,
    val readAt: Instant?
)

data class AIRequest(
    val id: String,
    val userId: String,
    val promptText: String,
    val contextType: String?,
    val createdAt: Instant
)

data class AISuggestion(
    val id: String,
    val requestId: String,
    val type: AISuggestionType,
    val suggestionText: String,
    val confidenceScore: Double?,
    val createdAt: Instant
)

data class AssistantConversationMessage(
    val id: String,
    val role: AssistantMessageRole,
    val text: String,
    val createdAt: Instant
)

data class AssistantContext(
    val sourceRoute: String?,
    val matchId: String?,
    val reservationId: String?,
    val venueId: String?
)

data class AssistantStructuredRequest(
    val rawText: String,
    val intent: AssistantIntent,
    val sportType: String?,
    val preferredDateTime: Instant?,
    val desiredPlayers: Int?,
    val venuePreference: String?,
    val targetMatchId: String?,
    val context: AssistantContext?
)

data class AssistantSuggestionItem(
    val id: String,
    val type: AISuggestionType,
    val title: String,
    val description: String?
)

data class AssistantVenueSuggestion(
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val sportType: String,
    val timeSlotId: String,
    val timeSlotLabel: String,
    val startTime: Instant,
    val endTime: Instant,
    val slotCapacity: Int?,
    val isPreferredTime: Boolean,
    val reason: String
)

data class AssistantGeneratedMessage(
    val kind: AssistantGeneratedMessageKind,
    val title: String,
    val content: String,
    val variants: List<String>
)

data class AssistantActionProposal(
    val id: String,
    val type: AssistantActionType,
    val title: String,
    val summary: String,
    val requiresConfirmation: Boolean,
    val venueId: String?,
    val timeSlotId: String?,
    val sportType: String?,
    val requiredPlayers: Int?,
    val scheduledStartTime: Instant?,
    val reservationId: String?,
    val description: String?
)

data class AssistantActionExecutionResult(
    val success: Boolean,
    val message: String,
    val createdReservationId: String?,
    val createdMatchId: String?
)

data class AssistantQuickAction(
    val id: String,
    val title: String,
    val prompt: String
)

data class AssistantReply(
    val text: String,
    val intent: AssistantIntent,
    val parsedRequest: AssistantStructuredRequest?,
    val suggestions: List<AssistantSuggestionItem>,
    val venueSuggestions: List<AssistantVenueSuggestion>,
    val generatedMessage: AssistantGeneratedMessage?,
    val actionProposal: AssistantActionProposal?,
    val quickActions: List<AssistantQuickAction>,
    val providerLabel: String,
    val usedFallback: Boolean
)

data class SuggestedAction(
    val id: String,
    val type: MonitoringSuggestedActionType,
    val title: String,
    val description: String?
)

data class ReschedulingSuggestion(
    val id: String,
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val timeSlotId: String,
    val timeSlotLabel: String,
    val startTime: Instant,
    val endTime: Instant,
    val reason: String
)

data class MatchMonitoringResult(
    val matchId: String,
    val matchTitle: String,
    val sportType: String,
    val venueName: String,
    val scheduledStartTime: Instant,
    val status: MatchReadinessStatus,
    val reason: String,
    val summary: String,
    val requiredPlayers: Int,
    val invitedPlayersCount: Int,
    val confirmedPlayersCount: Int,
    val pendingPlayersCount: Int,
    val declinedPlayersCount: Int,
    val remainingSpots: Int,
    val minutesUntilMatch: Long,
    val shouldAlertOrganizer: Boolean,
    val suggestedActions: List<SuggestedAction>,
    val reschedulingSuggestions: List<ReschedulingSuggestion>,
    val reminderMessageText: String?,
    val updateMessageText: String?
)

data class MonitoringAlert(
    val id: String,
    val matchId: String,
    val title: String,
    val body: String,
    val status: MatchReadinessStatus,
    val createdAt: Instant,
    val summary: String,
    val suggestedActions: List<SuggestedAction>,
    val reschedulingSuggestions: List<ReschedulingSuggestion>,
    val reminderMessageText: String?,
    val updateMessageText: String?
)

data class UserWithProfiles(
    val user: User,
    val organizer: Organizer?,
    val player: Player?
)

data class VenueWithTimeSlots(
    val venue: Venue,
    val slots: List<TimeSlot>
)

data class MatchWithInvitations(
    val match: Match,
    val invitations: List<Invitation>
)
