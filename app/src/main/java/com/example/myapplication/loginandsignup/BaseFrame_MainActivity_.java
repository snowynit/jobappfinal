package com.example.myapplication.loginandsignup;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.notifications.NotificationHelper;
import com.example.myapplication.notifications.ReminderScheduler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseFrame_MainActivity_ extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._base_constantframe);

        NotificationHelper.ensureChannels(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            ReminderScheduler.scheduleFromPrefs(this);
            functions.moveByType(this, user.getUid());
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.baseframe, new SigninOrSignupChoiceButtons()).commit();
        }
    }
}
