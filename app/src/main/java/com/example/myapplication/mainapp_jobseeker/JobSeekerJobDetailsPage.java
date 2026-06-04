package com.example.myapplication.mainapp_jobseeker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class JobSeekerJobDetailsPage extends Fragment {

    private Job job;
    private ImageButton likeButton;
    private Button applyButton;
    private TextView statusText;

    private boolean isLiked;
    private boolean isApplied;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._item_details_job_details_page, container, false);
        job = functions.jobFromBundle(requireArguments());

        TextView businessName = pageui.findViewById(R.id.businessName);
        TextView title = pageui.findViewById(R.id.title);
        TextView type = pageui.findViewById(R.id.type);
        TextView pay = pageui.findViewById(R.id.pay);
        TextView address = pageui.findViewById(R.id.address);
        TextView phone = pageui.findViewById(R.id.phone);
        TextView email = pageui.findViewById(R.id.email);
        TextView description = pageui.findViewById(R.id.description);
        likeButton = pageui.findViewById(R.id.likeButton);
        applyButton = pageui.findViewById(R.id.applyButton);
        statusText = pageui.findViewById(R.id.statusText);

        businessName.setText(functions.getValue(job.businessName, "Business"));
        title.setText(functions.getValue(job.title, "Open Position"));
        type.setText(job.type == null || job.type.trim().isEmpty() ? "Open role" : job.type.replace("_", " ").trim());
        pay.setText(job.pay == null || job.pay.trim().isEmpty() ? "Pay not provided" : (job.pay.contains("₪") ? job.pay : "₪" + job.pay + "/hr"));
        address.setText(functions.getValue(job.address, job.location, "Address not provided"));
        phone.setText(functions.getValue(job.phone, "Phone number not provided"));
        email.setText(functions.getValue(job.email, "Email not provided"));
        description.setText(functions.getValue(job.description, "No description available yet."));

        likeButton.setOnClickListener(v -> toggleLike());
        applyButton.setOnClickListener(v -> applyToJob());

        loadCurrentState();

        return pageui;
    }

    private void loadCurrentState() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("liked_jobs").document(job.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLiked = documentSnapshot.exists();
                    updateLikeButton();
                });

        db.collection("applications").document(userId + "_" + job.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isApplied = documentSnapshot.exists();
                    if (documentSnapshot.exists()) {
                        job.status = documentSnapshot.getString("status");
                    }
                    updateApplyButton();
                });
    }

    private void toggleLike() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Updating saved jobs...");

        FirebaseFirestore likesRef = FirebaseFirestore.getInstance();

        if (isLiked) {
            likesRef.collection("users").document(userId).collection("liked_jobs").document(job.id)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        functions.hideLoading(loading);
                        isLiked = false;
                        updateLikeButton();
                        Toast.makeText(getContext(), "Removed from liked jobs.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> functions.hideLoading(loading));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("jobId", job.id);
        data.put("title", job.title);
        data.put("description", job.description);
        data.put("type", job.type);
        data.put("pay", job.pay);
        data.put("businessName", job.businessName);
        data.put("location", job.location);
        data.put("address", job.address);
        data.put("phone", job.phone);
        data.put("email", job.email);
        data.put("actionType", "Liked");
        data.put("likedAt", FieldValue.serverTimestamp());

        likesRef.collection("users").document(userId).collection("liked_jobs").document(job.id)
                .set(data)
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    isLiked = true;
                    updateLikeButton();
                    Toast.makeText(getContext(), "Added to liked jobs.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> functions.hideLoading(loading));
    }

    private void applyToJob() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isApplied) {
            Toast.makeText(getContext(), "You already applied to this job.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Sending application...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            String applicantName = functions.getValue(
                    userDoc.getString("FullName"),
                    userDoc.getString("Name"),
                    "Applicant"
            );
            String resumeUri = functions.getValue(userDoc.getString("ResumeUri"), "");
            String resumeName = functions.getValue(userDoc.getString("ResumeName"), "");
            String resumeText = functions.getValue(userDoc.getString("ResumeText"), "");
            String resumeSource = functions.getValue(userDoc.getString("ResumeSource"), "");

            if (resumeUri.isEmpty() && resumeText.isEmpty()) {
                functions.hideLoading(loading);
                Toast.makeText(getContext(), "Upload a resume or make one in profile first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (resumeName.isEmpty()) {
                resumeName = resumeUri.isEmpty() ? "In-app resume" : "Resume file";
            }
            if (resumeSource.isEmpty()) {
                resumeSource = resumeUri.isEmpty() ? "builder" : "upload";
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("jobId", job.id);
            data.put("businessId", functions.getValue(job.ownerId, job.id));
            data.put("applicantName", applicantName);
            data.put("applicantId", userId);
            data.put("title", job.title);
            data.put("description", job.description);
            data.put("type", job.type);
            data.put("pay", job.pay);
            data.put("businessName", job.businessName);
            data.put("location", job.location);
            data.put("address", job.address);
            data.put("phone", job.phone);
            data.put("email", job.email);
            data.put("status", "Pending");
            data.put("resumeUri", resumeUri);
            data.put("resumeName", resumeName);
            data.put("resumeText", resumeText);
            data.put("resumeSource", resumeSource);
            data.put("appliedAt", FieldValue.serverTimestamp());

            db.collection("applications").document(userId + "_" + job.id)
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        functions.hideLoading(loading);
                        isApplied = true;
                        job.status = "Pending";
                        updateApplyButton();
                        Toast.makeText(getContext(), "Application sent.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        functions.hideLoading(loading);
                        Toast.makeText(getContext(), "Failed to send application.", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            functions.hideLoading(loading);
            Toast.makeText(getContext(), "Failed to load your profile.", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLikeButton() {
        likeButton.setBackgroundColor(Color.TRANSPARENT);
        likeButton.setImageResource(isLiked ? R.drawable.heartf : R.drawable.hearte);
    }

    private void updateApplyButton() {
        applyButton.setText(isApplied ? "Applied" : "Apply with Resume");
        applyButton.setEnabled(!isApplied);

        if (isApplied) {
            statusText.setVisibility(View.VISIBLE);
            String status = functions.getValue(job.status, "Pending");
            statusText.setText("Status: " + status);
            applyStatusColor(status);
        } else {
            statusText.setVisibility(View.GONE);
        }
    }

    private void applyStatusColor(String status) {
        String lowered = status.toLowerCase();
        if (lowered.contains("accept")) {
            statusText.setTextColor(Color.parseColor("#166534"));
        } else if (lowered.contains("reject")) {
            statusText.setTextColor(Color.parseColor("#B91C1C"));
        } else {
            statusText.setTextColor(Color.parseColor("#92400E"));
        }
    }
}
