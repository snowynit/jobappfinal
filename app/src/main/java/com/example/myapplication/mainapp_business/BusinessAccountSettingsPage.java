package com.example.myapplication.mainapp_business;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.loginandsignup.BaseFrame_MainActivity_;
import com.example.myapplication.settings.ChangePasswordDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BusinessAccountSettingsPage extends Fragment {

    private EditText companyNameInput;
    private EditText emailInput;
    private EditText phoneInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_account_settings_page, container, false);

        companyNameInput = pageui.findViewById(R.id.etBusinessName);
        emailInput = pageui.findViewById(R.id.etEmail);
        phoneInput = pageui.findViewById(R.id.etPhone);
        Button saveButton = pageui.findViewById(R.id.saveAccountChanges);
        Button changePasswordButton = pageui.findViewById(R.id.btnChangePassword);
        Button logoutButton = pageui.findViewById(R.id.logout);

        loadAccountInfo();

        saveButton.setOnClickListener(v -> saveAccountInfo());
        changePasswordButton.setOnClickListener(v ->
                new ChangePasswordDialog().show(getParentFragmentManager(), "business_change_password_dialog"));
        logoutButton.setOnClickListener(v -> showLogoutDialog());

        return pageui;
    }

    private void loadAccountInfo() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        androidx.appcompat.app.AlertDialog loading = functions.showLoading(this, "Loading account info...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    functions.hideLoading(loading);
                    companyNameInput.setText(functions.getValue(document.getString("Business Name"), document.getString("Name")));
                    emailInput.setText(functions.getValue(document.getString("Business Email"), document.getString("Email")));
                    phoneInput.setText(functions.getValue(document.getString("Business Phone"), document.getString("PhoneNumber")));
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to load account info.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveAccountInfo() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String businessName = companyNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (businessName.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Business name and email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("Account Type", "Business");
        data.put("Business Name", businessName);
        data.put("Business Email", email);
        data.put("Business Phone", phone);
        data.put("Email", email);
        androidx.appcompat.app.AlertDialog loading = functions.showLoading(this, "Saving account settings...");

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> syncJobContactInfo(userId, businessName, email, phone, loading))
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to save account info.", Toast.LENGTH_SHORT).show();
                });
    }

    private void syncJobContactInfo(String userId, String businessName, String email, String phone, androidx.appcompat.app.AlertDialog loading) {
        Map<String, Object> update = new HashMap<>();
        update.put("businessName", businessName);
        update.put("email", email);
        update.put("phone", phone);

        FirebaseFirestore.getInstance().collection("jobs")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    functions.hideLoading(loading);
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        document.getReference().set(update, SetOptions.merge());
                    }
                    Toast.makeText(getContext(), "Account settings updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Account settings updated.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            functions.logout(this);
            Intent intent = new Intent(getActivity(), BaseFrame_MainActivity_.class);
            startActivity(intent);
            requireActivity().finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
