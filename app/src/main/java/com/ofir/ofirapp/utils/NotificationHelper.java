package com.ofir.ofirapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.screens.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "ofir_app_notifications";
    private static final String CHANNEL_NAME = "General Notifications";
    private static int notificationId = 0;
    
    private final NotificationManager notificationManager;
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("General notifications for OfirApp");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent getDefaultIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public void showNotification(String title, String message, NotificationType type) {
        // Get default notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Create notification with high priority
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setContentIntent(getDefaultIntent())
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(notificationId++, builder.build());
    }

    public void showLoginSuccess(String username) {
        showNotification(
                "Welcome Back!",
                "Successfully logged in as " + username,
                NotificationType.SUCCESS
        );
    }

    public void showLoginError(String message) {
        showNotification(
                "Login Failed",
                message,
                NotificationType.ERROR
        );
    }

    public enum NotificationType {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }
} 