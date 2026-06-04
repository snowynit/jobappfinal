package com.example.myapplication.mainapp_jobseeker;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.functions;

public class MainAppJobSeeker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._base_constantframe_main);
        ImageView home = findViewById(R.id.homeBtn);
        ImageView settings = findViewById(R.id.setttingsBtn);
        ImageView search = findViewById(R.id.searchBtn);
        ImageView application = findViewById(R.id.applicationsBtn);
        ImageView post = findViewById(R.id.addBtn);

        // First fragment inside main app
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, new JobSeekerHomePage())
                .commit();

        home.setOnClickListener(v -> {
            functions.moveApp(this, new JobSeekerHomePage());
        });
        search.setOnClickListener(v -> {
            functions.moveApp(this, new JobSeekerSearchPage());
        });
        post.setOnClickListener(v -> {
            functions.moveApp(this, new JobSeekerPostPage());
        });
        application.setOnClickListener(v -> {
            functions.moveApp(this, new JobSeekerApplicationsPage());
        });
        settings.setOnClickListener(v -> {
            functions.moveApp(this, new JobSeekerSettingsPage());
        });
    }
}
