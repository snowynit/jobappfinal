package com.example.myapplication.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AccountSettings extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._settings_accountsettings, container, false);

        Button logout, changePassword, save;
        EditText name, email, phone;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        name = pageui.findViewById(R.id.etFullName);
        email = pageui.findViewById(R.id.etEmail);
        phone = pageui.findViewById(R.id.etPhone);

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return pageui;
        }
        String uid = auth.getCurrentUser().getUid();
        androidx.appcompat.app.AlertDialog loading = functions.showLoading(this, "Loading account info...");

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    functions.hideLoading(loading);

                    if (documentSnapshot.exists()) {

                        // Get data safely
                        String fullName = documentSnapshot.getString("FullName");
                        String useremail = documentSnapshot.getString("Email");
                        String userphone = documentSnapshot.getString("PhoneNumber");

                        // Set values (with null checks)
                        name.setText(fullName != null ? fullName : "");
                        email.setText(useremail != null ? useremail : "");
                        phone.setText(userphone != null ? userphone : "");

                    } else {
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                });

        logout = pageui.findViewById(R.id.logout);
        changePassword = pageui.findViewById(R.id.btnChangePassword);
        save = pageui.findViewById(R.id.saveAccountChanges);

        save.setOnClickListener(v -> saveAccountInfo(auth, db, name, email, phone));

        logout.setOnClickListener(v -> {
            AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
            builder.setTitle("Logout?");
            builder.setPositiveButton("Logout", (dialog, which) ->  {
                functions.logout(this);
                Intent intent = new Intent(getActivity(), BaseFrame_MainActivity_.class);
                startActivity(intent);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
        changePassword.setOnClickListener(v ->
                new ChangePasswordDialog().show(getParentFragmentManager(), "change_password_dialog"));
        return pageui;
    }

    private void saveAccountInfo(FirebaseAuth auth, FirebaseFirestore db, EditText name, EditText email, EditText phone) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = name.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();

        if (fullName.isEmpty() || emailText.isEmpty()) {
            Toast.makeText(getContext(), "Name and email are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(getContext(), "Enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("FullName", fullName);
        data.put("Name", fullName);
        data.put("Email", emailText);
        data.put("PhoneNumber", phoneText);
        data.put("Account Type", "Job Seeker");
        data.put("AccountType", "Job Seeker");

        androidx.appcompat.app.AlertDialog loading = functions.showLoading(this, "Saving account info...");
        db.collection("users").document(auth.getCurrentUser().getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Account settings updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to save account info.", Toast.LENGTH_SHORT).show();
                });
    }
}
