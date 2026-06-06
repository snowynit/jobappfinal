package com.example.myapplication.mainapp_jobseeker;

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
import com.example.myapplication.settings.AccountSettings;
import com.example.myapplication.settings.Applied;
import com.example.myapplication.settings.HelpCenter;
import com.example.myapplication.settings.Notifications;
import com.example.myapplication.settings.PrivacyPolicy;
import com.example.myapplication.settings.ProfileInfo;

public class JobSeekerSettingsPage extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._js_nav_settingspage, container, false);
        CardView accountset, profileinfo, applied, notifications, location, privacy, help;

        accountset = pageui.findViewById(R.id.accountSettings);
        profileinfo = pageui.findViewById(R.id.profileInfo);
        applied = pageui.findViewById(R.id.applied);
        notifications = pageui.findViewById(R.id.notifications);
        privacy = pageui.findViewById(R.id.privacyPolicy);
        help = pageui.findViewById(R.id.helpCenter);

        accountset.setOnClickListener(v -> {
            functions.moveMain(this, new AccountSettings());
        });
        profileinfo.setOnClickListener(v -> {
            functions.moveMain(this, new ProfileInfo());
        });
        applied.setOnClickListener(v -> {
            functions.moveMain(this, new Applied());
        });
        notifications.setOnClickListener(v -> {
            new Notifications().show(getParentFragmentManager(), "notifications_dialog");
        });

        privacy.setOnClickListener(v -> {
            functions.moveMain(this, new PrivacyPolicy());
        });
        help.setOnClickListener(v -> {
            functions.moveMain(this, new HelpCenter());
        });
//        Button logout;
//
//        logout = pageui.findViewById(R.id.logout);
//
//        logout.setOnClickListener(v -> {
//
//            AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
//            builder.setTitle("Logout?");
//            builder.setPositiveButton("Logout", (dialog, which) ->  {
//                functions.logout(this);
//                Intent intent = new Intent(getActivity(), BaseFrame_MainActivity_.class);
//                startActivity(intent);
//            });
//            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//            builder.show();
//        });
        return pageui;
    }
}
