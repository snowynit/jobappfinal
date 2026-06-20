package com.example.myapplication.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    // נקרא אחרי reboot של המכשיר — מחזיר את ההתראה היומית (כי alarms נמחקים בכיבוי)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && !"android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())) {
            return;
        }
        NotificationHelper.ensureChannels(context);
        ReminderScheduler.scheduleFromPrefs(context);
    }
}
