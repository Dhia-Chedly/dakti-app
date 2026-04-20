package com.dakti.app.integration.dialer

import android.content.Intent
import android.net.Uri
import com.dakti.app.integration.DialerPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialerIntegration @Inject constructor() {

    fun buildIntent(payload: DialerPayload): Intent? {
        val sanitizedNumber = payload.phoneNumber
            .filter { char -> char.isDigit() || char == '+' }
            .takeIf { value -> value.isNotBlank() }
            ?: return null

        return Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:$sanitizedNumber")
        )
    }
}
