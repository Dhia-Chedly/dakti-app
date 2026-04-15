package com.dakti.app.di

import android.content.Context
import android.content.SharedPreferences
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.local.session.SharedPreferencesSessionLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionLocalDataSource(
        impl: SharedPreferencesSessionLocalDataSource
    ): SessionLocalDataSource

    companion object {
        private const val SESSION_PREFS_FILE: String = "dakti_session_prefs"

        @Provides
        @Singleton
        fun provideSharedPreferences(
            @ApplicationContext context: Context
        ): SharedPreferences {
            return context.getSharedPreferences(SESSION_PREFS_FILE, Context.MODE_PRIVATE)
        }
    }
}
