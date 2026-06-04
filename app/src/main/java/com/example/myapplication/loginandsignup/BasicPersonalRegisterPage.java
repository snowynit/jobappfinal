package com.example.myapplication.loginandsignup;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.MainAppJobSeeker;

import java.util.HashMap;
import java.util.Map;

public class BasicPersonalRegisterPage extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_personal_register__third_a_, container, false);

        TextView showPassword = pageui.findViewById(R.id.ShowPassword);
        TextView alert = pageui.findViewById(R.id.alert);
        TextView needed = pageui.findViewById(R.id.information);
        EditText password = pageui.findViewById(R.id.Password);
        EditText confirm = pageui.findViewById(R.id.ConfirmPassword);
        EditText name = pageui.findViewById(R.id.Name);
        EditText email = pageui.findViewById(R.id.Email);
        EditText phone = pageui.findViewById(R.id.PhoneNumber);
        Button next = pageui.findViewById(R.id.next);
        ImageView info = pageui.findViewById(R.id.info);

        showPassword.setPaintFlags(showPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        showPassword.setOnClickListener(view -> {
            boolean isHidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (isHidden) {
                functions.show(password, confirm);
                showPassword.setText("Hide Password");
            } else {
                functions.hide(password, confirm);
                showPassword.setText("Show Password");
            }
        });

        next.setOnClickListener(view -> {
            if (!functions.passwordmatch(password, confirm)) {
                functions.alert(alert);
                return;
            }
            alert.setVisibility(View.GONE);

            if (!functions.Personalfilled(name, email)) {
                Toast.makeText(getActivity(), "Fill All The Information!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("FullName", name.getText().toString().trim());
            data.put("PhoneNumber", phone.getText().toString().trim());
            data.put("Account Type", "Job Seeker");
            data.put("AccountType", "Job Seeker");
            data.put("Email", email.getText().toString().trim());

            AlertDialog loading = functions.showLoading(this, "Creating your account...");
            functions.register(email.getText().toString().trim(), password.getText().toString(), data, task -> {
                functions.hideLoading(loading);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getActivity(), MainAppJobSeeker.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Could not create account. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }, auth -> {
                if (!auth.isSuccessful()) {
                    functions.hideLoading(loading);
                    String message = auth.getException() != null ? auth.getException().getMessage() : "Signup failed";
                    alert.setText(message);
                    alert.setVisibility(View.VISIBLE);
                }
            });
        });

        info.setOnClickListener(view ->
                needed.setVisibility(needed.getVisibility() == View.GONE ? View.VISIBLE : View.GONE)
        );

        return pageui;
    }
}
