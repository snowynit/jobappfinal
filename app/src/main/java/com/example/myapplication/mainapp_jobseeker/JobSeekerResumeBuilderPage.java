package com.example.myapplication.mainapp_jobseeker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class JobSeekerResumeBuilderPage extends Fragment {

    private EditText fullNameInput;
    private EditText headlineInput;
    private EditText phoneInput;
    private EditText emailInput;
    private EditText locationInput;
    private EditText summaryInput;
    private EditText skillsInput;
    private EditText experienceInput;
    private EditText educationInput;
    private EditText languagesInput;
    private TextView previewText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._js_resume_builder_page, container, false);

        fullNameInput = pageui.findViewById(R.id.resumeFullName);
        headlineInput = pageui.findViewById(R.id.resumeHeadline);
        phoneInput = pageui.findViewById(R.id.resumePhone);
        emailInput = pageui.findViewById(R.id.resumeEmail);
        locationInput = pageui.findViewById(R.id.resumeLocation);
        summaryInput = pageui.findViewById(R.id.resumeSummary);
        skillsInput = pageui.findViewById(R.id.resumeSkills);
        experienceInput = pageui.findViewById(R.id.resumeExperience);
        educationInput = pageui.findViewById(R.id.resumeEducation);
        languagesInput = pageui.findViewById(R.id.resumeLanguages);
        previewText = pageui.findViewById(R.id.resumePreviewText);
        Button saveButton = pageui.findViewById(R.id.saveResumeButton);
        Button useProfileButton = pageui.findViewById(R.id.useProfileInfoButton);

        TextWatcher watcher = new SimpleTextWatcher(this::updatePreview);
        fullNameInput.addTextChangedListener(watcher);
        headlineInput.addTextChangedListener(watcher);
        phoneInput.addTextChangedListener(watcher);
        emailInput.addTextChangedListener(watcher);
        locationInput.addTextChangedListener(watcher);
        summaryInput.addTextChangedListener(watcher);
        skillsInput.addTextChangedListener(watcher);
        experienceInput.addTextChangedListener(watcher);
        educationInput.addTextChangedListener(watcher);
        languagesInput.addTextChangedListener(watcher);

        useProfileButton.setOnClickListener(v -> fillFromProfile());
        saveButton.setOnClickListener(v -> saveResume());

        loadExistingResume();

        return pageui;
    }

    private void loadExistingResume() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading resume...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    functions.hideLoading(loading);
                    if (!document.exists()) {
                        updatePreview();
                        return;
                    }

                    fullNameInput.setText(functions.getValue(
                            document.getString("ResumeBuilderFullName"),
                            document.getString("FullName"),
                            document.getString("Name")
                    ));
                    headlineInput.setText(document.getString("ResumeBuilderHeadline"));
                    phoneInput.setText(document.getString("ResumeBuilderPhone"));
                    emailInput.setText(functions.getValue(
                            document.getString("ResumeBuilderEmail"),
                            document.getString("Email")
                    ));
                    locationInput.setText(document.getString("ResumeBuilderLocation"));
                    summaryInput.setText(functions.getValue(
                            document.getString("ResumeBuilderSummary"),
                            document.getString("Bio")
                    ));
                    skillsInput.setText(functions.getValue(
                            document.getString("ResumeBuilderSkills"),
                            document.getString("PreferredFields")
                    ));
                    experienceInput.setText(document.getString("ResumeBuilderExperience"));
                    educationInput.setText(document.getString("ResumeBuilderEducation"));
                    languagesInput.setText(document.getString("ResumeBuilderLanguages"));
                    updatePreview();
                })
                .addOnFailureListener(e -> functions.hideLoading(loading));
    }

    private void fillFromProfile() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(getContext(), "Profile info was not found yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (fullNameInput.getText().toString().trim().isEmpty()) {
                        fullNameInput.setText(functions.getValue(document.getString("FullName"), document.getString("Name")));
                    }
                    if (headlineInput.getText().toString().trim().isEmpty()) {
                        headlineInput.setText(functions.getValue(document.getString("PreferredFields"), ""));
                    }
                    if (emailInput.getText().toString().trim().isEmpty()) {
                        emailInput.setText(functions.getValue(document.getString("Email"), ""));
                    }
                    if (summaryInput.getText().toString().trim().isEmpty()) {
                        summaryInput.setText(functions.getValue(document.getString("Bio"), ""));
                    }
                    if (skillsInput.getText().toString().trim().isEmpty()) {
                        skillsInput.setText(functions.getValue(document.getString("PreferredFields"), ""));
                    }
                    updatePreview();
                    Toast.makeText(getContext(), "Loaded what we could from your profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveResume() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = fullNameInput.getText().toString().trim();
        String summary = summaryInput.getText().toString().trim();
        String experience = experienceInput.getText().toString().trim();
        String education = educationInput.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(getContext(), "Add your full name first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (summary.isEmpty() || experience.isEmpty() || education.isEmpty()) {
            Toast.makeText(getContext(), "Add your summary, experience, and education.", Toast.LENGTH_SHORT).show();
            return;
        }

        String resumeText = buildResumePreview();

        Map<String, Object> data = new HashMap<>();
        data.put("ResumeBuilderFullName", fullName);
        data.put("ResumeBuilderHeadline", headlineInput.getText().toString().trim());
        data.put("ResumeBuilderPhone", phoneInput.getText().toString().trim());
        data.put("ResumeBuilderEmail", emailInput.getText().toString().trim());
        data.put("ResumeBuilderLocation", locationInput.getText().toString().trim());
        data.put("ResumeBuilderSummary", summary);
        data.put("ResumeBuilderSkills", skillsInput.getText().toString().trim());
        data.put("ResumeBuilderExperience", experience);
        data.put("ResumeBuilderEducation", education);
        data.put("ResumeBuilderLanguages", languagesInput.getText().toString().trim());
        data.put("ResumeText", resumeText);
        data.put("ResumeName", "In-app resume");
        data.put("ResumeSource", "builder");
        data.put("ResumeUri", "");
        AlertDialog loading = functions.showLoading(this, "Saving resume...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Resume saved and ready to apply.", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to save resume.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePreview() {
        String preview = buildResumePreview();
        if (preview.isEmpty()) {
            previewText.setText("Your resume preview will appear here as you type.");
        } else {
            previewText.setText(preview);
        }
    }

    private String buildResumePreview() {
        return functions.buildResumeText(
                fullNameInput.getText().toString().trim(),
                headlineInput.getText().toString().trim(),
                phoneInput.getText().toString().trim(),
                emailInput.getText().toString().trim(),
                locationInput.getText().toString().trim(),
                summaryInput.getText().toString().trim(),
                skillsInput.getText().toString().trim(),
                experienceInput.getText().toString().trim(),
                educationInput.getText().toString().trim(),
                languagesInput.getText().toString().trim()
        );
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onChange.run();
        }
    }
}
