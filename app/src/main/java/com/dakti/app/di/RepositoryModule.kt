package com.dakti.app.di

import com.dakti.app.data.repository.AssistantRepositoryImpl
import com.dakti.app.data.repository.AuthRepositoryImpl
import com.dakti.app.data.repository.InvitationRepositoryImpl
import com.dakti.app.data.repository.MatchRepositoryImpl
import com.dakti.app.data.repository.NotificationRepositoryImpl
import com.dakti.app.data.repository.ReservationRepositoryImpl
import com.dakti.app.data.repository.VenueRepositoryImpl
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.domain.repository.VenueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVenueRepository(impl: VenueRepositoryImpl): VenueRepository

    @Binds
    @Singleton
    abstract fun bindReservationRepository(impl: ReservationRepositoryImpl): ReservationRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindInvitationRepository(impl: InvitationRepositoryImpl): InvitationRepository

    @Binds
    @Singleton
    abstract fun bindAssistantRepository(impl: AssistantRepositoryImpl): AssistantRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
