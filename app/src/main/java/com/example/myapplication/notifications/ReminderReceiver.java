package com.example.myapplication.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_REMINDER = "com.example.myapplication.ACTION_DAILY_REMINDER";

    // נקרא כש-AlarmManager שולח Broadcast — בודק מי המשתמש ושולח התראה מתאימה
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = NotificationHelper.prefs(context);
        if (!prefs.getBoolean(NotificationHelper.KEY_REMINDERS_ENABLED, true)) {
            return;
        }

        Context appContext = context.getApplicationContext();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            post(appContext,
                    "Time to check in",
                    "Open the app to see fresh job matches and pending applications.");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    boolean isBusiness = doc.exists()
                            && ("business".equalsIgnoreCase(doc.getString("AccountType"))
                                || hasBusinessName(doc.getString("Business Name")));

                    if (isBusiness) {
                        post(appContext,
                                "Applicants are waiting",
                                "Review pending candidates and keep your job postings fresh.");
                    } else {
                        post(appContext,
                                "New roles match your profile",
                                "Open the app to see today's recommended jobs and follow up on applications.");
                    }
                })
                .addOnFailureListener(e -> post(appContext,
                        "Time to check in",
                        "Open the app to see fresh job matches and pending applications."));
    }

    private static boolean hasBusinessName(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void post(Context context, String title, String body) {
        NotificationHelper.post(
                context,
                NotificationHelper.CHANNEL_REMINDERS,
                NotificationHelper.NOTIF_ID_REMINDER,
                title,
                body
        );
    }
}
