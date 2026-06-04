package com.example.myapplication.mainapp_business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.settings.HelpCenter;
import com.example.myapplication.settings.Notifications;
import com.example.myapplication.settings.PrivacyPolicy;

public class BusinessSettingsPage extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_settings_page, container, false);

        CardView accountSettings = pageui.findViewById(R.id.accountSettings);
        CardView companyProfile = pageui.findViewById(R.id.companyProfile);
        CardView myJobs = pageui.findViewById(R.id.myJobs);
        CardView applicants = pageui.findViewById(R.id.applicants);
        CardView notifications = pageui.findViewById(R.id.notifications);
        CardView privacyPolicy = pageui.findViewById(R.id.privacyPolicy);
        CardView helpCenter = pageui.findViewById(R.id.helpCenter);

        accountSettings.setOnClickListener(v -> functions.moveMain(this, new BusinessAccountSettingsPage()));
        companyProfile.setOnClickListener(v -> functions.moveMain(this, new BusinessProfilePage()));
        myJobs.setOnClickListener(v -> functions.moveMain(this, new BusinessJobsPage()));
        applicants.setOnClickListener(v -> functions.moveMain(this, new BusinessApplicantsPage()));
        notifications.setOnClickListener(v -> new Notifications().show(getParentFragmentManager(), "business_notifications_dialog"));
        privacyPolicy.setOnClickListener(v -> functions.moveMain(this, new PrivacyPolicy()));
        helpCenter.setOnClickListener(v -> functions.moveMain(this, new HelpCenter()));

        return pageui;
    }
}
