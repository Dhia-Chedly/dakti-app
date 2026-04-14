package com.dakti.app.domain.model

data class AppUser(
    val id: String,
    val displayName: String,
    val email: String
)

data class Venue(
    val id: String,
    val name: String,
    val sportType: String
)

data class Reservation(
    val id: String,
    val venueName: String,
    val timeSlot: String
)

data class Match(
    val id: String,
    val title: String,
    val status: String
)

data class Invitation(
    val id: String,
    val matchTitle: String,
    val fromUser: String
)
