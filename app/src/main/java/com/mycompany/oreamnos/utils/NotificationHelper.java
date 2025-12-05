package com.mycompany.oreamnos.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.mycompany.oreamnos.R;

/**
 * Helper class for managing notifications during content generation.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "oreamnos_generation_channel";
    private static final int NOTIFICATION_ID = 1001;
    public static final int FOREGROUND_NOTIFICATION_ID = 1002;
    private static final int AUTO_DISMISS_DELAY_MS = 3000;

    private final Context context;
    private final NotificationManager notificationManager;
    private final Handler handler;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    /**
     * Creates the notification channel for Android 8.0+.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(context.getString(R.string.notification_channel_desc));
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Shows a progress notification with indeterminate progress bar.
     * 
     * @param title   Notification title
     * @param message Notification message
     */
    public void showProgressNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setProgress(0, 0, true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Updates the notification to show completion status.
     * Auto-dismisses after a delay.
     * 
     * @param title   Notification title
     * @param message Notification message
     */
    public void showCompletedNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Auto-dismiss after delay
        handler.postDelayed(this::dismissNotification, AUTO_DISMISS_DELAY_MS);
    }

    /**
     * Shows an error notification.
     * 
     * @param title   Notification title
     * @param message Error message
     */
    public void showErrorNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Auto-dismiss after delay
        handler.postDelayed(this::dismissNotification, AUTO_DISMISS_DELAY_MS);
    }

    /**
     * Dismisses the notification.
     */
    public void dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Dismisses the foreground service notification.
     */
    public void dismissForegroundNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID);
    }

    /**
     * Builds a notification suitable for Foreground Service.
     * This notification will be shown while content generation is in progress.
     *
     * @param title   Notification title
     * @param message Notification message
     * @return Built Notification object
     */
    public Notification buildForegroundNotification(String title, String message) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setProgress(0, 0, true)
                .build();
    }
}
