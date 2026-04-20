package com.dakti.app.integration.email

import android.content.Intent
import android.net.Uri
import com.dakti.app.integration.EmailPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailIntegration @Inject constructor() {

    fun buildIntent(payload: EmailPayload): Intent? {
        if (payload.subject.isBlank() && payload.body.isBlank()) {
            return null
        }

        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            if (payload.recipients.isNotEmpty()) {
                putExtra(Intent.EXTRA_EMAIL, payload.recipients.toTypedArray())
            }
            putExtra(Intent.EXTRA_SUBJECT, payload.subject)
            putExtra(Intent.EXTRA_TEXT, payload.body)
        }
    }
}
