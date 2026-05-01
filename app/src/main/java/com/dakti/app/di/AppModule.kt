package com.dakti.app.di

import com.dakti.app.ai.service.AiAssistantService
import com.dakti.app.ai.service.DemoAiAssistantService
import com.dakti.app.ai.service.SupabaseEdgeAiAssistantService
import com.dakti.app.util.AppConstants
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
    fun provideAiAssistantService(
        supabaseEdgeAiAssistantService: SupabaseEdgeAiAssistantService
    ): AiAssistantService {
        return if (AppConstants.IS_SUPABASE_CONFIGURED) {
            supabaseEdgeAiAssistantService
        } else {
            DemoAiAssistantService()
        }
    }
}
