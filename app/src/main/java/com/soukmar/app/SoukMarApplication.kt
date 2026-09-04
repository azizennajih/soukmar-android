package com.soukmar.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SoukMarApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            "Notifications SoukMar",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
