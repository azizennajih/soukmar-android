package com.soukmar.app.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.soukmar.app.MainActivity
import com.soukmar.app.R
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.repository.PushRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/** onMessageReceived only ever fires while the app process is alive
 * (foreground, or background-but-not-killed with a data-only payload) —
 * for a killed app, or a backgrounded app receiving our notification+data
 * payload, the system tray auto-displays it without calling this. So the
 * manual NotificationCompat build below only runs for the foreground case,
 * with no risk of double-showing what the system already displayed. */
@AndroidEntryPoint
class SoukMarFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var pushRepository: PushRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            if (tokenManager.isLoggedIn()) pushRepository.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: ""
        val url = message.data["url"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_notifications", true)
            url?.let { putExtra("notification_url", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (canPost) {
            NotificationManagerCompat.from(this).notify(notificationIdCounter.incrementAndGet(), notification)
        }
    }

    companion object {
        private val notificationIdCounter = AtomicInteger(1000)
    }
}
