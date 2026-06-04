package com.example.myapplication.mainapp_business;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.LocationPickerDialog;
import com.example.myapplication.media.CameraSupport;

import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BusinessProfilePage extends Fragment implements LocationPickerDialog.LocationSelectionListener {

    private ImageView logoPreview;
    private EditText companyNameInput;
    private EditText companyDescriptionInput;
    private EditText companySizeInput;
    private EditText companyAddressInput;
    private EditText companyEmailInput;
    private EditText companyPhoneInput;
    private TextView locationSummary;
    private String logoUri = "";
    private boolean pickedNewLogo = false;

    private double businessLat = 0d;
    private double businessLng = 0d;

    private final CameraSupport photo = new CameraSupport(this, uri -> {
        String localUri = functions.pickImage(requireContext(), uri, logoPreview, R.drawable.pfp);
        if (localUri.isEmpty()) {
            Toast.makeText(getContext(), "Could not load this picture. Try another one.", Toast.LENGTH_SHORT).show();
            return;
        }
        pickedNewLogo = true;
        logoUri = localUri;
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_profile_page, container, false);

        logoPreview = pageui.findViewById(R.id.companyLogoPreview);
        companyNameInput = pageui.findViewById(R.id.companyNameInput);
        companyDescriptionInput = pageui.findViewById(R.id.companyDescriptionInput);
        companySizeInput = pageui.findViewById(R.id.companySizeInput);
        companyAddressInput = pageui.findViewById(R.id.companyAddressInput);
        companyEmailInput = pageui.findViewById(R.id.companyEmailInput);
        companyPhoneInput = pageui.findViewById(R.id.companyPhoneInput);
        View logoUploadArea = pageui.findViewById(R.id.logoUploadArea);
        Button saveButton = pageui.findViewById(R.id.saveCompanyProfileButton);
        locationSummary = pageui.findViewById(R.id.businessLocationSummary);
        Button setLocationButton = pageui.findViewById(R.id.setBusinessLocationButton);

        logoUploadArea.setOnClickListener(v -> photo.show());
        saveButton.setOnClickListener(v -> saveProfile());
        setLocationButton.setOnClickListener(v -> openLocationPicker());

        loadProfile();

        return pageui;
    }

    private void loadProfile() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading company profile...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    functions.hideLoading(loading);
                    companyNameInput.setText(functions.getValue(document.getString("Business Name"), ""));
                    companyDescriptionInput.setText(functions.getValue(document.getString("Business Description"), ""));
                    companySizeInput.setText(functions.getValue(document.getString("Business Size"), ""));
                    companyAddressInput.setText(functions.getValue(document.getString("Business Address"), ""));
                    companyEmailInput.setText(functions.getValue(document.getString("Business Email"), ""));
                    companyPhoneInput.setText(functions.getValue(document.getString("Business Phone"), ""));
                    if (!pickedNewLogo) {
                        logoUri = functions.getValue(document.getString("Business Logo Uri"), "");
                        functions.loadImageUri(logoPreview, logoUri, R.drawable.pfp);
                    }

                    Double lat = document.getDouble("Business Latitude");
                    Double lng = document.getDouble("Business Longitude");
                    if (lat != null && lng != null && !(lat == 0d && lng == 0d)) {
                        businessLat = lat;
                        businessLng = lng;
                        updateLocationSummary();
                    }
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to load company profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String businessName = companyNameInput.getText().toString().trim();
        if (businessName.isEmpty()) {
            Toast.makeText(getContext(), "Add your business name first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("Account Type", "Business");
        data.put("Business Name", businessName);
        data.put("Business Description", companyDescriptionInput.getText().toString().trim());
        data.put("Business Size", companySizeInput.getText().toString().trim());
        data.put("Business Address", companyAddressInput.getText().toString().trim());
        data.put("Business Email", companyEmailInput.getText().toString().trim());
        data.put("Business Phone", companyPhoneInput.getText().toString().trim());
        data.put("Business Logo Uri", logoUri);
        data.put("Business Latitude", businessLat);
        data.put("Business Longitude", businessLng);
        AlertDialog loading = functions.showLoading(this, "Saving company profile...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> syncExistingJobs(userId, businessName, loading))
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to save company profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void syncExistingJobs(String userId, String businessName, AlertDialog loading) {
        Map<String, Object> update = new HashMap<>();
        update.put("businessName", businessName);
        update.put("address", companyAddressInput.getText().toString().trim());
        update.put("location", companyAddressInput.getText().toString().trim());
        update.put("email", companyEmailInput.getText().toString().trim());
        update.put("phone", companyPhoneInput.getText().toString().trim());
        update.put("latitude", businessLat);
        update.put("longitude", businessLng);

        FirebaseFirestore.getInstance().collection("jobs")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    functions.hideLoading(loading);
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        document.getReference().set(update, SetOptions.merge());
                    }
                    Toast.makeText(getContext(), "Company profile updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Company profile updated.", Toast.LENGTH_SHORT).show();
                });
    }

    private void openLocationPicker() {
        Double lat = (businessLat == 0d && businessLng == 0d) ? null : businessLat;
        Double lng = (businessLat == 0d && businessLng == 0d) ? null : businessLng;
        LocationPickerDialog.newInstance(lat, lng, 10d)
                .show(getChildFragmentManager(), "business_location_picker");
    }

    private void updateLocationSummary() {
        if (locationSummary == null) {
            return;
        }
        if (businessLat == 0d && businessLng == 0d) {
            locationSummary.setText("Location not set");
        } else {
            locationSummary.setText(String.format(Locale.US, "Saved: %.5f, %.5f", businessLat, businessLng));
        }
    }

    @Override
    public void onLocationSelected(double latitude, double longitude, double radiusKm) {
        businessLat = latitude;
        businessLng = longitude;
        updateLocationSummary();
        Toast.makeText(getContext(), "Location set. Tap Save to apply.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationCleared() {
        businessLat = 0d;
        businessLng = 0d;
        updateLocationSummary();
    }
}
