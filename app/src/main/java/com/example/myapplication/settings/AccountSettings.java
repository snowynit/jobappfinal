package com.example.myapplication.settings;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountSettings extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._settings_accountsettings, container, false);

        Button logout, changePassword;
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
}
