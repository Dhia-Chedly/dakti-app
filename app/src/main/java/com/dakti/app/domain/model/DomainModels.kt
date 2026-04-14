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
    DRAFT,
    OPEN,
    FULL,
    CONFIRMED,
    CANCELLED,
    COMPLETED
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
    GENERAL
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
