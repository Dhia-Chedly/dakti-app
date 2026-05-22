package com.dakti.app.domain.location

data class UserLocation(
    val latitude: Double,
    val longitude: Double
)

interface UserLocationProvider {
    suspend fun getCurrentLocation(): UserLocation?
}
