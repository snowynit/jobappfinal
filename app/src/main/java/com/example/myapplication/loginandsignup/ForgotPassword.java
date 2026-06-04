package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.util.Patterns;
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

public class ForgotPassword extends Fragment {
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_forgot_password, container, false);

        EditText emailInput = pageui.findViewById(R.id.email);
        TextView alertText = pageui.findViewById(R.id.alert);
        Button resetButton = pageui.findViewById(R.id.resetBtn);
        TextView backToLogin = pageui.findViewById(R.id.backToLogin);

        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                alertText.setVisibility(View.VISIBLE);
                alertText.setText("Please enter your email.");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                alertText.setVisibility(View.VISIBLE);
                alertText.setText("Please enter a valid email address.");
                return;
            }

            alertText.setVisibility(View.GONE);
            resetButton.setEnabled(false);
            resetButton.setText("Sending...");
            AlertDialog loading = functions.showLoading(this, "Sending reset link...");

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        functions.hideLoading(loading);
                        resetButton.setEnabled(true);
                        resetButton.setText("Send Reset Link");
                        Toast.makeText(getContext(), "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                        functions.move(this, new LoginPage(), null);
                    })
                    .addOnFailureListener(e -> {
                        functions.hideLoading(loading);
                        resetButton.setEnabled(true);
                        resetButton.setText("Send Reset Link");
                        alertText.setVisibility(View.VISIBLE);
                        alertText.setText(e.getMessage() == null ? "Failed to send reset email." : e.getMessage());
                    });
        });

        backToLogin.setOnClickListener(v -> functions.move(this, new LoginPage(), null));

        return pageui;
    }
}
