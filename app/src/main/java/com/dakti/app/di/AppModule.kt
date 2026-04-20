package com.dakti.app.di

import com.dakti.app.ai.service.AiAssistantService
import com.dakti.app.ai.service.DemoAiAssistantService
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
}
