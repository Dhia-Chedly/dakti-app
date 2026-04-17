package com.dakti.app.data.mapper

import com.dakti.app.data.local.entity.AIRequestEntity
import com.dakti.app.data.local.entity.AISuggestionEntity
import com.dakti.app.data.local.entity.InvitationEntity
import com.dakti.app.data.local.entity.MatchEntity
import com.dakti.app.data.local.entity.MatchWithContextRelation
import com.dakti.app.data.local.entity.MatchWithInvitationsRelation
import com.dakti.app.data.local.entity.NotificationEntity
import com.dakti.app.data.local.entity.OrganizerEntity
import com.dakti.app.data.local.entity.PlayerEntity
import com.dakti.app.data.local.entity.ReservationEntity
import com.dakti.app.data.local.entity.ReservationWithDetailsRelation
import com.dakti.app.data.local.entity.TimeSlotEntity
import com.dakti.app.data.local.entity.UserEntity
import com.dakti.app.data.local.entity.UserWithProfilesRelation
import com.dakti.app.data.local.entity.VenueEntity
import com.dakti.app.data.local.entity.VenueWithTimeSlotsRelation
import com.dakti.app.domain.model.AIRequest
import com.dakti.app.domain.model.AISuggestion
import com.dakti.app.domain.model.Invitation
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.domain.model.Notification
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserWithProfiles
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE HH:mm")
private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun UserEntity.toDomain(): User =
    User(
        id = id,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        role = role,
        bio = bio,
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant()
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        role = role,
        bio = bio,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )

fun OrganizerEntity.toDomain(): Organizer =
    Organizer(
        userId = userId,
        rating = rating,
        totalHostedMatches = totalHostedMatches,
        organizationName = organizationName,
        isVerified = isVerified,
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant()
    )

fun Organizer.toEntity(): OrganizerEntity =
    OrganizerEntity(
        userId = userId,
        rating = rating,
        totalHostedMatches = totalHostedMatches,
        organizationName = organizationName,
        isVerified = isVerified,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )

fun PlayerEntity.toDomain(): Player =
    Player(
        userId = userId,
        preferredSport = preferredSport,
        availabilityNote = availabilityNote,
        skillLevel = skillLevel,
        rating = rating,
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant()
    )

fun Player.toEntity(): PlayerEntity =
    PlayerEntity(
        userId = userId,
        preferredSport = preferredSport,
        availabilityNote = availabilityNote,
        skillLevel = skillLevel,
        rating = rating,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )

fun UserWithProfilesRelation.toDomain(): UserWithProfiles =
    UserWithProfiles(
        user = user.toDomain(),
        organizer = organizer?.toDomain(),
        player = player?.toDomain()
    )

fun VenueEntity.toDomain(): Venue =
    Venue(
        id = id,
        name = name,
        sportType = sportType,
        description = description,
        address = address,
        contactPhone = contactPhone,
        imageUrl = imageUrl,
        city = city,
        state = state,
        country = country,
        latitude = latitude,
        longitude = longitude,
        pricePerHour = pricePerHour,
        currency = currency,
        amenities = amenitiesCsv.toAmenityList(),
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant()
    )

fun Venue.toEntity(): VenueEntity =
    VenueEntity(
        id = id,
        name = name,
        sportType = sportType,
        description = description,
        address = address,
        contactPhone = contactPhone,
        imageUrl = imageUrl,
        city = city,
        state = state,
        country = country,
        latitude = latitude,
        longitude = longitude,
        pricePerHour = pricePerHour,
        currency = currency,
        amenitiesCsv = amenities.toAmenityCsv(),
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )

fun TimeSlotEntity.toDomain(): TimeSlot =
    TimeSlot(
        id = id,
        venueId = venueId,
        startTime = startTime.toInstant(),
        endTime = endTime.toInstant(),
        isAvailable = isAvailable,
        capacity = capacity
    )

fun TimeSlot.toEntity(): TimeSlotEntity =
    TimeSlotEntity(
        id = id,
        venueId = venueId,
        startTime = startTime.toEpochMillis(),
        endTime = endTime.toEpochMillis(),
        isAvailable = isAvailable,
        capacity = capacity
    )

fun VenueWithTimeSlotsRelation.toDomain(): VenueWithTimeSlots =
    VenueWithTimeSlots(
        venue = venue.toDomain(),
        slots = slots.map { it.toDomain() }
    )

fun ReservationEntity.toDomain(
    venueName: String = venueId,
    timeSlotLabel: String = timeSlotId
): Reservation =
    Reservation(
        id = id,
        organizerId = organizerId,
        venueId = venueId,
        timeSlotId = timeSlotId,
        venueName = venueName,
        timeSlot = timeSlotLabel,
        status = status,
        totalPrice = totalPrice,
        currency = currency,
        note = note,
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant()
    )

fun Reservation.toEntity(): ReservationEntity =
    ReservationEntity(
        id = id,
        organizerId = organizerId,
        venueId = venueId,
        timeSlotId = timeSlotId,
        status = status,
        totalPrice = totalPrice,
        currency = currency,
        note = note,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )

fun ReservationWithDetailsRelation.toDomain(): Reservation =
    reservation.toDomain(
        venueName = venue.name,
        timeSlotLabel = formatTimeSlot(timeSlot.startTime.toInstant(), timeSlot.endTime.toInstant())
    )

fun MatchEntity.toDomain(): Match =
    Match(
        id = id,
        organizerId = organizerId,
        venueId = venueId,
        reservationId = reservationId,
        title = title,
        sportType = sportType,
        scheduledStartTime = scheduledStartTime.toInstant(),
        requiredPlayers = requiredPlayers,
        status = status,
        description = description,
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant()
    )

fun Match.toEntity(): MatchEntity =
    MatchEntity(
        id = id,
        organizerId = organizerId,
        venueId = venueId,
        reservationId = reservationId,
        title = title,
        sportType = sportType,
        scheduledStartTime = scheduledStartTime.toEpochMillis(),
        requiredPlayers = requiredPlayers,
        status = status,
        description = description,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )

fun InvitationEntity.toDomain(
    matchTitle: String = "Match $matchId",
    fromUser: String = invitedByOrganizerId ?: "Organizer"
): Invitation =
    Invitation(
        id = id,
        matchId = matchId,
        playerId = playerId,
        invitedByOrganizerId = invitedByOrganizerId,
        matchTitle = matchTitle,
        fromUser = fromUser,
        status = status,
        message = message,
        sentAt = sentAt.toInstant(),
        respondedAt = respondedAt?.toInstant()
    )

fun Invitation.toEntity(): InvitationEntity =
    InvitationEntity(
        id = id,
        matchId = matchId,
        playerId = playerId,
        invitedByOrganizerId = invitedByOrganizerId,
        status = status,
        message = message,
        sentAt = sentAt.toEpochMillis(),
        respondedAt = respondedAt?.toEpochMillis()
    )

fun MatchWithInvitationsRelation.toDomain(): MatchWithInvitations =
    MatchWithInvitations(
        match = match.toDomain(),
        invitations = invitations.map { invitation ->
            invitation.toDomain(matchTitle = match.title, fromUser = invitation.invitedByOrganizerId ?: match.organizerId)
        }
    )

fun MatchWithContextRelation.toDomain(
    organizerName: String?
): MatchWithContext =
    MatchWithContext(
        match = match.toDomain(),
        venueName = venue.name,
        venueAddress = venue.address,
        reservationReference = reservation?.id,
        organizerName = organizerName,
        confirmedPlayersCount = invitations.count { invitation ->
            invitation.status == InvitationResponseStatus.ACCEPTED
        }
    )

fun NotificationEntity.toDomain(): Notification =
    Notification(
        id = id,
        userId = userId,
        type = type,
        title = title,
        content = content,
        isRead = isRead,
        relatedMatchId = relatedMatchId,
        relatedReservationId = relatedReservationId,
        createdAt = createdAt.toInstant(),
        readAt = readAt?.toInstant()
    )

fun Notification.toEntity(): NotificationEntity =
    NotificationEntity(
        id = id,
        userId = userId,
        type = type,
        title = title,
        content = content,
        isRead = isRead,
        relatedMatchId = relatedMatchId,
        relatedReservationId = relatedReservationId,
        createdAt = createdAt.toEpochMillis(),
        readAt = readAt?.toEpochMillis()
    )

fun AIRequestEntity.toDomain(): AIRequest =
    AIRequest(
        id = id,
        userId = userId,
        promptText = promptText,
        contextType = contextType,
        createdAt = createdAt.toInstant()
    )

fun AIRequest.toEntity(): AIRequestEntity =
    AIRequestEntity(
        id = id,
        userId = userId,
        promptText = promptText,
        contextType = contextType,
        createdAt = createdAt.toEpochMillis()
    )

fun AISuggestionEntity.toDomain(): AISuggestion =
    AISuggestion(
        id = id,
        requestId = requestId,
        type = type,
        suggestionText = suggestionText,
        confidenceScore = confidenceScore,
        createdAt = createdAt.toInstant()
    )

fun AISuggestion.toEntity(): AISuggestionEntity =
    AISuggestionEntity(
        id = id,
        requestId = requestId,
        type = type,
        suggestionText = suggestionText,
        confidenceScore = confidenceScore,
        createdAt = createdAt.toEpochMillis()
    )

private fun Long.toInstant(): Instant = Instant.ofEpochMilli(this)

private fun Instant.toEpochMillis(): Long = toEpochMilli()

private fun List<String>.toAmenityCsv(): String =
    joinToString(separator = "|") { value -> value.trim() }

private fun String.toAmenityList(): List<String> =
    split("|")
        .map { value -> value.trim() }
        .filter { value -> value.isNotEmpty() }

private fun formatTimeSlot(start: Instant, end: Instant): String {
    val zoneId = ZoneId.systemDefault()
    val startDateTime = start.atZone(zoneId)
    val endDateTime = end.atZone(zoneId)
    return "${startDateTime.format(slotStartFormatter)} - ${endDateTime.format(slotEndFormatter)}"
}
