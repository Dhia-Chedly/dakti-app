package com.dakti.app.data.remote.dto

data class VenueDto(
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
    val amenities: List<String>
)

data class TimeSlotDto(
    val id: String,
    val venueId: String,
    val startTimeIso: String,
    val endTimeIso: String,
    val isAvailable: Boolean,
    val capacity: Int?
)

data class VenueDetailsDto(
    val venue: VenueDto,
    val timeSlots: List<TimeSlotDto>
)
