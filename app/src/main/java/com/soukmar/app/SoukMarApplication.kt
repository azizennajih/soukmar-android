package com.soukmar.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

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

        // osmdroid needs a one-time global config (tile cache dir + a
        // user agent — OSM's tile servers reject requests without one).
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
    }
}
