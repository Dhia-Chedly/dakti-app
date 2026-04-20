package com.dakti.app.integration.maps

import android.content.Intent
import android.net.Uri
import com.dakti.app.integration.VenueLocationPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsIntegration @Inject constructor() {

    fun buildGoogleMapsIntent(payload: VenueLocationPayload): Intent? {
        val geoUri = buildGeoUri(payload) ?: return null
        return Intent(Intent.ACTION_VIEW, geoUri)
            .setPackage(GOOGLE_MAPS_PACKAGE)
    }

    fun buildGenericMapsIntent(payload: VenueLocationPayload): Intent? {
        val geoUri = buildGeoUri(payload) ?: return null
        return Intent(Intent.ACTION_VIEW, geoUri)
    }

    private fun buildGeoUri(payload: VenueLocationPayload): Uri? {
        val latitude = payload.latitude
        val longitude = payload.longitude
        if (latitude != null && longitude != null) {
            val label = payload.venueName?.ifBlank { "Venue" } ?: "Venue"
            val encodedLabel = Uri.encode(label)
            return Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        }

        val address = payload.address?.trim().orEmpty()
        if (address.isBlank()) {
            return null
        }

        val query = listOfNotNull(
            payload.venueName?.trim()?.takeIf { value -> value.isNotBlank() },
            address
        ).joinToString(separator = ", ")

        return Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    }

    private companion object {
        private const val GOOGLE_MAPS_PACKAGE: String = "com.google.android.apps.maps"
    }
}
