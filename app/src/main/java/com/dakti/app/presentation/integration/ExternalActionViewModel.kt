package com.dakti.app.presentation.integration

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dakti.app.integration.CalendarEventPayload
import com.dakti.app.integration.DialerPayload
import com.dakti.app.integration.EmailPayload
import com.dakti.app.integration.IntegrationManager
import com.dakti.app.integration.ShareMessagePayload
import com.dakti.app.integration.VenueLocationPayload
import com.dakti.app.integration.toUserMessageOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExternalActionViewModel @Inject constructor(
    private val integrationManager: IntegrationManager
) : ViewModel() {

    fun launchWhatsApp(
        context: Context,
        payload: ShareMessagePayload
    ): String? = integrationManager.launchWhatsApp(context, payload).toUserMessageOrNull()

    fun launchEmail(
        context: Context,
        payload: EmailPayload
    ): String? = integrationManager.launchEmail(context, payload).toUserMessageOrNull()

    fun launchDialer(
        context: Context,
        payload: DialerPayload
    ): String? = integrationManager.launchDialer(context, payload).toUserMessageOrNull()

    fun launchMaps(
        context: Context,
        payload: VenueLocationPayload
    ): String? = integrationManager.launchMaps(context, payload).toUserMessageOrNull()

    fun launchCalendar(
        context: Context,
        payload: CalendarEventPayload
    ): String? = integrationManager.launchCalendar(context, payload).toUserMessageOrNull()
}
