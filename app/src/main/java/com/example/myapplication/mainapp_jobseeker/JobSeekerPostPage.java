package com.example.myapplication.mainapp_jobseeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.media.CameraSupport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class JobSeekerPostPage extends Fragment {

    private ImageView uploadPreview;
    private EditText nameInput;
    private EditText aboutInput;
    private EditText payInput;
    private EditText fieldsInput;
    private TextView resumeNameText;
    private TextView resumeHintText;
    private String selectedType = "";
    private String profileImageUri = "";
    private String resumeUri = "";
    private String resumeName = "";
    private boolean pickedNewProfileImage = false;

    private Button fullTimeButton;
    private Button partTimeButton;
    private Button temporaryButton;

    private final CameraSupport photo = new CameraSupport(this, uri -> {
        String localUri = functions.pickImage(requireContext(), uri, uploadPreview, R.drawable.pfp);
        if (localUri.isEmpty()) {
            Toast.makeText(getContext(), "Could not load this picture. Try another one.", Toast.LENGTH_SHORT).show();
            return;
        }
        pickedNewProfileImage = true;
        profileImageUri = localUri;
    });

    private final ActivityResultLauncher<String[]> resumePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null || getContext() == null) {
                    return;
                }
                functions.persistReadPermission(requireContext(), uri);
                resumeUri = uri.toString();
                resumeName = functions.getDisplayName(requireContext(), uri);
                resumeNameText.setText(resumeName);
                if (resumeHintText != null) {
                    resumeHintText.setText("Uploaded file ready to send.");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._js_nav_postpage_jobseeker, container, false);

        uploadPreview = pageui.findViewById(R.id.imgUploadPreview);
        nameInput = pageui.findViewById(R.id.name);
        aboutInput = pageui.findViewById(R.id.about);
        payInput = pageui.findViewById(R.id.pay);
        fieldsInput = pageui.findViewById(R.id.fields);
        resumeNameText = pageui.findViewById(R.id.resumeName);
        resumeHintText = pageui.findViewById(R.id.resumeHint);
        fullTimeButton = pageui.findViewById(R.id.fulltime);
        partTimeButton = pageui.findViewById(R.id.parttime);
        temporaryButton = pageui.findViewById(R.id.temp);
        View uploadArea = pageui.findViewById(R.id.layoutUpload);
        Button uploadResume = pageui.findViewById(R.id.uploadResume);
        TextView buildResume = pageui.findViewById(R.id.buildResumeLink);
        Button saveProfile = pageui.findViewById(R.id.postProfile);

        uploadArea.setOnClickListener(v -> photo.show());
        uploadResume.setOnClickListener(v -> resumePickerLauncher.launch(new String[]{"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}));
        buildResume.setOnClickListener(v -> functions.moveMain(this, new JobSeekerResumeBuilderPage(), null));
        fullTimeButton.setOnClickListener(v -> selectType("Full Time"));
        partTimeButton.setOnClickListener(v -> selectType("Part Time"));
        temporaryButton.setOnClickListener(v -> selectType("Temporary"));
        saveProfile.setOnClickListener(v -> saveProfile());

        loadProfile();

        return pageui;
    }

    private void loadProfile() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading profile...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    functions.hideLoading(loading);
                    if (!document.exists()) {
                        functions.loadImageUri(uploadPreview, null, R.drawable.pfp);
                        resumeNameText.setText("No file selected");
                        resumeHintText.setText("Upload a file or build one inside the app.");
                        return;
                    }

                    nameInput.setText(functions.getValue(document.getString("FullName"), document.getString("Name")));
                    aboutInput.setText(document.getString("Bio"));
                    payInput.setText(functions.getValue(document.getString("DesiredPay"), ""));
                    fieldsInput.setText(functions.getValue(document.getString("PreferredFields"), ""));

                    if (!pickedNewProfileImage) {
                        profileImageUri = functions.getValue(document.getString("ProfileImageUri"), "");
                        functions.loadImageUri(uploadPreview, profileImageUri, R.drawable.pfp);
                    }
                    resumeUri = functions.getValue(document.getString("ResumeUri"), "");
                    resumeName = functions.getValue(document.getString("ResumeName"), "");
                    selectedType = functions.getValue(document.getString("PreferredType"), "");

                    resumeNameText.setText(functions.getResumeLabel(document));
                    updateResumeHint(document);
                    updateTypeButtons();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void selectType(String type) {
        if (type.equals(selectedType)) {
            selectedType = "";
        } else {
            selectedType = type;
        }
        updateTypeButtons();
    }

    private void updateTypeButtons() {
        functions.styleButton(fullTimeButton, "Full Time".equals(selectedType));
        functions.styleButton(partTimeButton, "Part Time".equals(selectedType));
        functions.styleButton(temporaryButton, "Temporary".equals(selectedType));
    }

    private void saveProfile() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nameInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please add your name.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("FullName", nameInput.getText().toString().trim());
        data.put("Bio", aboutInput.getText().toString().trim());
        data.put("DesiredPay", payInput.getText().toString().trim());
        data.put("PreferredFields", fieldsInput.getText().toString().trim());
        data.put("PreferredType", selectedType);
        data.put("ProfileImageUri", profileImageUri);
        data.put("ResumeUri", resumeUri);
        data.put("ResumeName", resumeName);
        if (!resumeUri.isEmpty()) {
            data.put("ResumeSource", "upload");
            data.put("ResumeText", "");
        }
        AlertDialog loading = functions.showLoading(this, "Saving profile...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to save profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateResumeHint(com.google.firebase.firestore.DocumentSnapshot document) {
        if (functions.hasUploadedResume(document)) {
            resumeHintText.setText("Uploaded file ready to send.");
        } else if (functions.hasBuiltResume(document)) {
            resumeHintText.setText("In-app resume ready to send.");
        } else {
            resumeHintText.setText("Upload a file or build one inside the app.");
        }
    }
}
