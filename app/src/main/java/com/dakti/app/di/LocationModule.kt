package com.dakti.app.di

import android.content.Context
import com.dakti.app.data.location.FusedUserLocationProvider
import com.dakti.app.domain.location.UserLocationProvider
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindUserLocationProvider(
        impl: FusedUserLocationProvider
    ): UserLocationProvider

    companion object {
        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
        ) = LocationServices.getFusedLocationProviderClient(context)
    }
}
