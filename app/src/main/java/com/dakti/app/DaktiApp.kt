package com.dakti.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dakti.app.notification.NotificationHelper
import com.dakti.app.worker.NotificationWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DaktiApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var notificationWorkScheduler: NotificationWorkScheduler

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
        notificationWorkScheduler.ensurePeriodicMatchMonitoring()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
