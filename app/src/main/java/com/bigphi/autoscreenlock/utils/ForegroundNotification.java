package com.bigphi.autoscreenlock.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ForegroundNotification {

    public static NotificationCompat.Builder getNotificationForBelowOrio(PendingIntent pendingIntent, Context context, String channedId) {
        NotificationCompat.Builder covid_tracker = new NotificationCompat.Builder(context, channedId)
                .setContentIntent(pendingIntent)
                .setContentText("00:00")
                .setContentTitle("Screen AutoLocker")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setChannelId(channedId);
        return covid_tracker;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification.Builder getNotificationAboveOrio(PendingIntent pendingIntent, Context context, String channedId) {
        setupNotificationChannel(context, channedId);
        Notification.Builder covid_tracker = new Notification.Builder(context, channedId)
                .setContentText("00:00")
                .setContentTitle("Screen AutoLocker")
                .setContentIntent(pendingIntent)
                .setChannelId(channedId)
                .setOnlyAlertOnce(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.sym_action_chat);
        return covid_tracker;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void setupNotificationChannel(Context context, String channedId) {
        NotificationManager notificationManager=(NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(channedId,"name", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public static String convertToPrintableText(long minute, long second) {
        String minuteInString = String.valueOf(minute);
        String secondInString = String.valueOf(second);
        StringBuilder builder = new StringBuilder();
        if(minuteInString.length() == 1){
            builder.append("0");
        }
        builder.append(minuteInString).append(":");
        if(secondInString.length() == 1){
            builder.append("0");
        }
        builder.append(secondInString);
        return builder.toString();

    }
}
