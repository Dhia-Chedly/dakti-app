package com.dakti.app.data.location

import android.annotation.SuppressLint
import com.dakti.app.domain.location.UserLocation
import com.dakti.app.domain.location.UserLocationProvider
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FusedUserLocationProvider @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : UserLocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation? {
        return runCatching {
            val current = awaitCurrentLocation()
            if (current != null) {
                UserLocation(
                    latitude = current.latitude,
                    longitude = current.longitude
                )
            } else {
                val last = awaitLastLocation()
                if (last == null) {
                    null
                } else {
                    UserLocation(
                        latitude = last.latitude,
                        longitude = last.longitude
                    )
                }
            }
        }.getOrNull()
    }

    private suspend fun awaitCurrentLocation(): android.location.Location? {
        return suspendCancellableCoroutine { continuation ->
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build()
            fusedLocationProviderClient
                .getCurrentLocation(request, null)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }

    private suspend fun awaitLastLocation(): android.location.Location? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }
}
