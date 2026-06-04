package com.example.myapplication.mainapp_business;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.functions;


public class MainAppBusiness extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._base_constantframe_main);
        ImageView home = findViewById(R.id.homeBtn);
        ImageView jobs = findViewById(R.id.searchBtn);
        ImageView createJob = findViewById(R.id.addBtn);
        ImageView applicants = findViewById(R.id.applicationsBtn);
        ImageView profile = findViewById(R.id.setttingsBtn);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, new BusinessHomePage())
                .commit();

        home.setOnClickListener(v -> functions.moveApp(this, new BusinessHomePage()));
        jobs.setOnClickListener(v -> functions.moveApp(this, new BusinessCandidatesPage()));
        createJob.setOnClickListener(v -> functions.moveApp(this, new BusinessCreateJobPage()));
        applicants.setOnClickListener(v -> functions.moveApp(this, new BusinessApplicantsPage()));
        profile.setOnClickListener(v -> functions.moveApp(this, new BusinessSettingsPage()));
    }
}
