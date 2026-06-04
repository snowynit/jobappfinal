package com.example.myapplication.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class HelpCenter extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._settings_helpcenter, container, false);

        View contactSupport = pageui.findViewById(R.id.contactSupportCard);

        contactSupport.setOnClickListener(v -> Toast.makeText(
                requireContext(),
                "Support contact can be connected here later.",
                Toast.LENGTH_SHORT
        ).show());

        return pageui;
    }
}
