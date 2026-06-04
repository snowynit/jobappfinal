package com.example.myapplication.settings;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.notifications.NotificationHelper;
import com.example.myapplication.notifications.ReminderReceiver;
import com.example.myapplication.notifications.ReminderScheduler;

import java.util.Locale;

public class Notifications extends DialogFragment {

    private int reminderHour = 9;
    private int reminderMinute = 0;

    @Override
    public int getTheme() {
        return android.R.style.Theme_Translucent_NoTitleBar;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._item_notifications, container, false);

        SwitchCompat masterSwitch = pageui.findViewById(R.id.notificationsEnabled);
        SwitchCompat remindersSwitch = pageui.findViewById(R.id.remindersSwitch);
        TextView reminderTime = pageui.findViewById(R.id.reminderTimeValue);
        Button testReminder = pageui.findViewById(R.id.testReminderButton);
        Button cancel = pageui.findViewById(R.id.cancelButton);
        Button save = pageui.findViewById(R.id.saveButton);

        SharedPreferences prefs = NotificationHelper.prefs(requireContext());
        boolean masterEnabled = prefs.getBoolean(NotificationHelper.KEY_MASTER_ENABLED, true);
        boolean remindersEnabled = prefs.getBoolean(NotificationHelper.KEY_REMINDERS_ENABLED, true);
        reminderHour = prefs.getInt(NotificationHelper.KEY_REMINDER_HOUR, 9);
        reminderMinute = prefs.getInt(NotificationHelper.KEY_REMINDER_MINUTE, 0);

        masterSwitch.setChecked(masterEnabled);
        remindersSwitch.setChecked(remindersEnabled);
        remindersSwitch.setEnabled(masterEnabled);
        reminderTime.setText(formatTime(reminderHour, reminderMinute));
        reminderTime.setAlpha(masterEnabled ? 1f : 0.45f);

        masterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            remindersSwitch.setEnabled(isChecked);
            reminderTime.setAlpha(isChecked ? 1f : 0.45f);
        });

        reminderTime.setOnClickListener(v -> new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    reminderHour = hourOfDay;
                    reminderMinute = minute;
                    reminderTime.setText(formatTime(hourOfDay, minute));
                },
                reminderHour,
                reminderMinute,
                false
        ).show());

        testReminder.setOnClickListener(v -> {
            Context ctx = requireContext().getApplicationContext();
            Intent fire = new Intent(ctx, ReminderReceiver.class);
            fire.setAction(ReminderReceiver.ACTION_DAILY_REMINDER);
            ctx.sendBroadcast(fire);
            Toast.makeText(ctx, "Alarm fired — check the notification shade.", Toast.LENGTH_SHORT).show();
        });

        cancel.setOnClickListener(v -> dismiss());

        save.setOnClickListener(v -> {
            Context ctx = requireContext().getApplicationContext();
            boolean allOn = masterSwitch.isChecked();

            NotificationHelper.prefs(ctx)
                    .edit()
                    .putBoolean(NotificationHelper.KEY_MASTER_ENABLED, allOn)
                    .apply();

            ReminderScheduler.saveAndSchedule(
                    ctx,
                    allOn && remindersSwitch.isChecked(),
                    reminderHour,
                    reminderMinute
            );

            Toast.makeText(ctx, "Notification preferences saved.", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return pageui;
    }

    private static String formatTime(int hour, int minute) {
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        String suffix = hour < 12 ? "AM" : "PM";
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, suffix);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setDimAmount(0.6f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
