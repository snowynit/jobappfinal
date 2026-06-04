package com.example.myapplication.mainapp_business;

import android.os.Bundle;
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
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BusinessCreateJobPage extends Fragment {

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText addressInput;
    private EditText payInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button typeFull;
    private Button typePart;
    private Button typeTemp;
    private String selectedType = "";
    private Job editingJob;
    private String businessName = "";
    private double businessLat = 0d;
    private double businessLng = 0d;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_create_job_page, container, false);

        titleInput = pageui.findViewById(R.id.jobTitle);
        descriptionInput = pageui.findViewById(R.id.jobDescription);
        addressInput = pageui.findViewById(R.id.jobAddress);
        payInput = pageui.findViewById(R.id.jobPay);
        emailInput = pageui.findViewById(R.id.jobEmail);
        phoneInput = pageui.findViewById(R.id.jobPhone);
        typeFull = pageui.findViewById(R.id.typeFull);
        typePart = pageui.findViewById(R.id.typePart);
        typeTemp = pageui.findViewById(R.id.typeTemp);
        TextView pageTitle = pageui.findViewById(R.id.pageTitle);
        TextView pageSubtitle = pageui.findViewById(R.id.pageSubtitle);
        Button saveButton = pageui.findViewById(R.id.saveJobButton);

        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            editingJob = functions.jobFromBundle(args);
            pageTitle.setText("Edit Job");
            pageSubtitle.setText("Update the role so applicants always see the latest details.");
            saveButton.setText("Save Changes");
            fillJobForEdit();
        }

        typeFull.setOnClickListener(v -> selectType("Full Time"));
        typePart.setOnClickListener(v -> selectType("Part Time"));
        typeTemp.setOnClickListener(v -> selectType("Temporary"));
        saveButton.setOnClickListener(v -> saveJob());
        updateTypeButtons();

        loadBusinessDefaults();

        return pageui;
    }

    private void loadBusinessDefaults() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading business info...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    functions.hideLoading(loading);
                    businessName = functions.getValue(
                            document.getString("Business Name"),
                            editingJob == null ? "" : editingJob.businessName,
                            "My Business"
                    );
                    if (emailInput.getText().toString().trim().isEmpty()) {
                        emailInput.setText(functions.getValue(document.getString("Business Email"), ""));
                    }
                    if (phoneInput.getText().toString().trim().isEmpty()) {
                        phoneInput.setText(functions.getValue(document.getString("Business Phone"), ""));
                    }
                    if (addressInput.getText().toString().trim().isEmpty()) {
                        addressInput.setText(functions.getValue(document.getString("Business Address"), ""));
                    }

                    Double lat = document.getDouble("Business Latitude");
                    Double lng = document.getDouble("Business Longitude");
                    if (lat != null && lng != null) {
                        businessLat = lat;
                        businessLng = lng;
                    }
                })
                .addOnFailureListener(e -> functions.hideLoading(loading));
    }

    private void fillJobForEdit() {
        titleInput.setText(editingJob.title);
        descriptionInput.setText(editingJob.description);
        addressInput.setText(functions.getValue(editingJob.address, editingJob.location));
        payInput.setText(editingJob.pay);
        emailInput.setText(editingJob.email);
        phoneInput.setText(editingJob.phone);
        selectedType = functions.getValue(editingJob.type, "");
        updateTypeButtons();
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
        functions.styleButton(typeFull, "Full Time".equals(selectedType));
        functions.styleButton(typePart, "Part Time".equals(selectedType));
        functions.styleButton(typeTemp, "Temporary".equals(selectedType));
    }

    private void saveJob() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String pay = payInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || address.isEmpty() || pay.isEmpty() || selectedType.isEmpty()) {
            Toast.makeText(getContext(), "Fill the main job details first.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog loading = functions.showLoading(this, editingJob == null ? "Posting job..." : "Saving job...");

        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", userId);
        data.put("businessId", userId);
        data.put("businessName", functions.getValue(businessName, editingJob == null ? "" : editingJob.businessName, "My Business"));
        data.put("title", title);
        data.put("description", description);
        data.put("address", address);
        data.put("location", address);
        data.put("pay", pay);
        data.put("email", email);
        data.put("phone", phone);
        data.put("type", selectedType);
        data.put("latitude", businessLat);
        data.put("longitude", businessLng);
        data.put("jobState", editingJob == null ? "Active" : functions.getValue(editingJob.jobState, "Active"));
        data.put("jobOpen", !"Paused".equalsIgnoreCase(editingJob == null ? "Active" : editingJob.jobState));
        data.put("updatedAt", FieldValue.serverTimestamp());
        if (editingJob == null) {
            data.put("createdAt", FieldValue.serverTimestamp());
        }

        CollectionReference jobs = FirebaseFirestore.getInstance().collection("jobs");
        if (editingJob == null || editingJob.id == null || editingJob.id.trim().isEmpty()) {
            jobs.add(data)
                    .addOnSuccessListener(unused -> {
                        functions.hideLoading(loading);
                        Toast.makeText(getContext(), "Job posted.", Toast.LENGTH_SHORT).show();
                        functions.moveMain(this, new BusinessJobsPage(), null);
                    })
                    .addOnFailureListener(e -> {
                        functions.hideLoading(loading);
                        Toast.makeText(getContext(), "Failed to save job.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            jobs.document(editingJob.id)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        functions.hideLoading(loading);
                        Toast.makeText(getContext(), "Job updated.", Toast.LENGTH_SHORT).show();
                        functions.moveMain(this, new BusinessJobsPage(), null);
                    })
                    .addOnFailureListener(e -> {
                        functions.hideLoading(loading);
                        Toast.makeText(getContext(), "Failed to save job.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
