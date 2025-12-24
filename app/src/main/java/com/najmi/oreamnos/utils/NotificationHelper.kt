package com.najmi.oreamnos.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.najmi.oreamnos.R

/**
 * Helper class for managing notifications during content generation.
 */
class NotificationHelper(context: Context) {

    private val appContext: Context = context.applicationContext
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val handler = Handler(Looper.getMainLooper())

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for Android 8.0+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = appContext.getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a progress notification with indeterminate progress bar.
     */
    fun showProgressNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Updates the notification to show completion status.
     * Auto-dismisses after a delay.
     */
    fun showCompletedNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        handler.postDelayed({ dismissNotification() }, AUTO_DISMISS_DELAY_MS)
    }

    /**
     * Shows an error notification.
     */
    fun showErrorNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        handler.postDelayed({ dismissNotification() }, AUTO_DISMISS_DELAY_MS)
    }

    /**
     * Dismisses the notification.
     */
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Dismisses the foreground service notification.
     */
    fun dismissForegroundNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
    }

    /**
     * Builds a notification suitable for Foreground Service.
     */
    fun buildForegroundNotification(title: String, message: String): Notification {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "oreamnos_generation_channel"
        private const val NOTIFICATION_ID = 1001
        const val FOREGROUND_NOTIFICATION_ID = 1002
        private const val AUTO_DISMISS_DELAY_MS = 3000L
    }
}
