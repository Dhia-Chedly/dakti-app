package com.dakti.app.di

import android.content.Context
import androidx.room.Room
import com.dakti.app.data.local.dao.AssistantDao
import com.dakti.app.data.local.dao.InvitationDao
import com.dakti.app.data.local.dao.MatchDao
import com.dakti.app.data.local.dao.NotificationDao
import com.dakti.app.data.local.dao.ReservationDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideVenueDao(database: AppDatabase): VenueDao = database.venueDao()

    @Provides
    fun provideReservationDao(database: AppDatabase): ReservationDao = database.reservationDao()

    @Provides
    fun provideMatchDao(database: AppDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideInvitationDao(database: AppDatabase): InvitationDao = database.invitationDao()

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideAssistantDao(database: AppDatabase): AssistantDao = database.assistantDao()
}
