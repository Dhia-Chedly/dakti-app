package com.dakti.app.di

import com.dakti.app.ai.service.AiAssistantService
import com.dakti.app.ai.service.DemoAiAssistantService
import com.dakti.app.integration.IntegrationManager
import com.dakti.app.integration.calendar.CalendarIntegration
import com.dakti.app.integration.dialer.DialerIntegration
import com.dakti.app.integration.email.EmailIntegration
import com.dakti.app.integration.maps.MapsIntegration
import com.dakti.app.integration.whatsapp.WhatsAppIntegration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAiAssistantService(): AiAssistantService = DemoAiAssistantService()

    @Provides
    @Singleton
    fun provideWhatsAppIntegration(): WhatsAppIntegration = WhatsAppIntegration()

    @Provides
    @Singleton
    fun provideEmailIntegration(): EmailIntegration = EmailIntegration()

    @Provides
    @Singleton
    fun provideMapsIntegration(): MapsIntegration = MapsIntegration()

    @Provides
    @Singleton
    fun provideCalendarIntegration(): CalendarIntegration = CalendarIntegration()

    @Provides
    @Singleton
    fun provideDialerIntegration(): DialerIntegration = DialerIntegration()

    @Provides
    @Singleton
    fun provideIntegrationManager(
        whatsappIntegration: WhatsAppIntegration,
        emailIntegration: EmailIntegration,
        mapsIntegration: MapsIntegration,
        calendarIntegration: CalendarIntegration,
        dialerIntegration: DialerIntegration
    ): IntegrationManager {
        return IntegrationManager(
            whatsappIntegration = whatsappIntegration,
            emailIntegration = emailIntegration,
            mapsIntegration = mapsIntegration,
            calendarIntegration = calendarIntegration,
            dialerIntegration = dialerIntegration
        )
    }
}
