package com.dakti.app.data.remote.supabase.model

import com.google.gson.annotations.SerializedName

data class SupabaseAuthSignInRequest(
    val email: String,
    val password: String
)

data class SupabaseRefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class SupabaseAuthSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, Any?>? = null
)

data class SupabaseAuthUserResponse(
    val id: String,
    val email: String?,
    @SerializedName("user_metadata")
    val userMetadata: Map<String, Any?>? = null
)

data class SupabaseSessionResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    val user: SupabaseAuthUserResponse
)

data class SupabaseAuthSignUpResponse(
    val user: SupabaseAuthUserResponse?,
    val session: SupabaseSessionResponse?
)

data class ProfileRowDto(
    val id: String,
    val email: String,
    @SerializedName("full_name")
    val fullName: String,
    val phone: String?,
    val role: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @SerializedName("preferred_sport")
    val preferredSport: String?,
    @SerializedName("availability_note")
    val availabilityNote: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class VenueRowDto(
    val id: String,
    val name: String,
    @SerializedName("sport_type")
    val sportType: String,
    val address: String,
    val city: String?,
    val state: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("contact_number")
    val contactNumber: String?,
    val description: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("price_per_hour")
    val pricePerHour: Double?,
    val currency: String?,
    val amenities: List<String>?,
    val capacity: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class TimeSlotRowDto(
    val id: String,
    @SerializedName("venue_id")
    val venueId: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

data class ReservationRowDto(
    val id: String,
    @SerializedName("organizer_id")
    val organizerId: String,
    @SerializedName("venue_id")
    val venueId: String,
    @SerializedName("time_slot_id")
    val timeSlotId: String,
    val status: String,
    val notes: String?,
    @SerializedName("total_price")
    val totalPrice: Double?,
    val currency: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class MatchRowDto(
    val id: String,
    @SerializedName("organizer_id")
    val organizerId: String,
    @SerializedName("venue_id")
    val venueId: String,
    @SerializedName("reservation_id")
    val reservationId: String?,
    @SerializedName("sport_type")
    val sportType: String,
    @SerializedName("match_time")
    val matchTime: String,
    @SerializedName("required_players")
    val requiredPlayers: Int,
    val title: String?,
    val status: String,
    val description: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class InvitationRowDto(
    val id: String,
    @SerializedName("match_id")
    val matchId: String,
    @SerializedName("player_id")
    val playerId: String,
    @SerializedName("sender_id")
    val senderId: String,
    @SerializedName("message_text")
    val messageText: String?,
    @SerializedName("response_status")
    val responseStatus: String,
    @SerializedName("sent_at")
    val sentAt: String,
    @SerializedName("responded_at")
    val respondedAt: String?
)

data class AiRequestRowDto(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("request_text")
    val requestText: String,
    @SerializedName("request_type")
    val requestType: String?,
    @SerializedName("created_at")
    val createdAt: String
)

data class AiSuggestionRowDto(
    val id: String,
    @SerializedName("request_id")
    val requestId: String,
    @SerializedName("suggestion_type")
    val suggestionType: String,
    @SerializedName("suggestion_text")
    val suggestionText: String,
    val payload: Map<String, Any?>?,
    @SerializedName("created_at")
    val createdAt: String
)
