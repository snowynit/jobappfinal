package com.example.myapplication.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.Calendar;

public final class ReminderScheduler {

    private static final int REQUEST_CODE = 4242;

    private ReminderScheduler() {}

    // שומר את ההגדרות ומתזמן את ההתראה היומית
    public static void saveAndSchedule(Context context, boolean enabled, int hour, int minute) {
        SharedPreferences.Editor editor = NotificationHelper.prefs(context).edit();
        editor.putBoolean(NotificationHelper.KEY_REMINDERS_ENABLED, enabled);
        editor.putInt(NotificationHelper.KEY_REMINDER_HOUR, hour);
        editor.putInt(NotificationHelper.KEY_REMINDER_MINUTE, minute);
        editor.apply();

        if (enabled) {
            schedule(context, hour, minute);
        } else {
            cancel(context);
        }
    }

    // מתזמן את ההתראה לפי השעה ששמורה ב-SharedPreferences (נקרא בעלייה של האפליקציה ואחרי reboot)
    public static void scheduleFromPrefs(Context context) {
        SharedPreferences prefs = NotificationHelper.prefs(context);
        if (!prefs.getBoolean(NotificationHelper.KEY_REMINDERS_ENABLED, false)) {
            return;
        }
        int hour = prefs.getInt(NotificationHelper.KEY_REMINDER_HOUR, 9);
        int minute = prefs.getInt(NotificationHelper.KEY_REMINDER_MINUTE, 0);
        schedule(context, hour, minute);
    }

    // קובע ל-AlarmManager לירות כל יום בשעה שנבחרה
    private static void schedule(Context context, int hour, int minute) {
        AlarmManager alarmManager = ContextCompat.getSystemService(context, AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                next.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent(context)
        );
    }

    // מבטל את ההתראה היומית
    public static void cancel(Context context) {
        AlarmManager alarmManager = ContextCompat.getSystemService(context, AlarmManager.class);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent(context));
        }
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_DAILY_REMINDER);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }
}
