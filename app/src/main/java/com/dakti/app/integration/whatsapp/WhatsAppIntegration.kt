package com.dakti.app.integration.whatsapp

import android.content.Intent
import android.net.Uri
import com.dakti.app.integration.ShareMessagePayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppIntegration @Inject constructor() {

    fun buildIntent(payload: ShareMessagePayload): Intent? {
        val message = payload.text.trim()
        if (message.isBlank()) {
            return null
        }

        val encodedText = Uri.encode(message)
        val sanitizedPhone = payload.phoneNumber
            ?.filter { char -> char.isDigit() || char == '+' }
            ?.takeIf { value -> value.isNotBlank() }

        val deepLink = if (sanitizedPhone != null) {
            Uri.parse("https://wa.me/$sanitizedPhone?text=$encodedText")
        } else {
            Uri.parse("https://wa.me/?text=$encodedText")
        }

        return Intent(Intent.ACTION_VIEW, deepLink)
            .setPackage(WHATSAPP_PACKAGE)
    }

    fun buildShareFallbackIntent(payload: ShareMessagePayload): Intent? {
        val message = payload.text.trim()
        if (message.isBlank()) {
            return null
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }

    private companion object {
        private const val WHATSAPP_PACKAGE: String = "com.whatsapp"
    }
}
