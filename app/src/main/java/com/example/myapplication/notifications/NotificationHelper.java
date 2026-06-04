package com.example.myapplication.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.loginandsignup.BaseFrame_MainActivity_;

public final class NotificationHelper {

    public static final String CHANNEL_REMINDERS = "reminders";

    public static final String PREFS = "notification_prefs";
    public static final String KEY_MASTER_ENABLED = "master_enabled";
    public static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    public static final String KEY_REMINDER_HOUR = "reminder_hour";
    public static final String KEY_REMINDER_MINUTE = "reminder_minute";

    public static final int NOTIF_ID_REMINDER = 1001;

    private NotificationHelper() {}

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel reminders = new NotificationChannel(
                CHANNEL_REMINDERS, "Daily reminders", NotificationManager.IMPORTANCE_DEFAULT);
        reminders.setDescription("Daily nudge to check new jobs or applicants.");
        manager.createNotificationChannel(reminders);
    }

    public static void post(Context context, String channelId, int notifId, String title, String body) {
        ensureChannels(context);

        Intent open = new Intent(context, BaseFrame_MainActivity_.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, open, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (manager != null) {
            manager.notify(notifId, builder.build());
        }
    }

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
