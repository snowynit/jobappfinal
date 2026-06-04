package com.example.myapplication.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordDialog extends DialogFragment {

    @Override
    public int getTheme() {
        return android.R.style.Theme_Translucent_NoTitleBar;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._settings_change_password_dialog, container, false);

        EditText currentPassword = pageui.findViewById(R.id.currentPassword);
        EditText newPassword = pageui.findViewById(R.id.newPassword);
        EditText confirmPassword = pageui.findViewById(R.id.confirmPassword);
        Button cancel = pageui.findViewById(R.id.cancelButton);
        Button save = pageui.findViewById(R.id.saveButton);

        cancel.setOnClickListener(v -> dismiss());
        save.setOnClickListener(v -> submitPasswordChange(currentPassword, newPassword, confirmPassword));

        return pageui;
    }

    private void submitPasswordChange(EditText currentPasswordInput, EditText newPasswordInput, EditText confirmPasswordInput) {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Fill all password fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPassword.equals(newPassword)) {
            Toast.makeText(getContext(), "Choose a new password that is different.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(getContext(), "No email found for this account.", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.appcompat.app.AlertDialog loading = functions.showLoading(this, "Updating password...");

        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
                .addOnSuccessListener(unused ->
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(unused2 -> {
                                    functions.hideLoading(loading);
                                    Toast.makeText(getContext(), "Password changed successfully.", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    functions.hideLoading(loading);
                                    Toast.makeText(getContext(),
                                            e.getMessage() == null ? "Failed to update password." : e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(),
                            "Current password is incorrect.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setDimAmount(0.6f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
