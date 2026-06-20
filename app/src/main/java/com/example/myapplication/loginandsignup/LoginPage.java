package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_login_page__second_b_, container, false);
        Button signin = pageui.findViewById(R.id.signin);
        EditText email = pageui.findViewById(R.id.Email);
        EditText password = pageui.findViewById(R.id.Password);
        TextView showpass = pageui.findViewById(R.id.ShowPassword);
        TextView reset = pageui.findViewById(R.id.resetPassword);
        TextView alert = pageui.findViewById(R.id.alert);

        showpass.setOnClickListener(view -> {
            boolean isHidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (isHidden) {
                password.setTransformationMethod(null);
                showpass.setText("Hide Password");
            } else {
                password.setTransformationMethod(new PasswordTransformationMethod());
                showpass.setText("Show Password");
            }
        });

        reset.setOnClickListener(view -> {
            String emailValue = email.getText().toString().trim();
            functions.sendPasswordReset(this, emailValue, alert, reset, "Forgot password?");
        });

        signin.setOnClickListener(view -> {
            String emailValue = email.getText().toString().trim();
            String passwordValue = password.getText().toString().trim();

            if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                alert.setVisibility(View.VISIBLE);
                alert.setText("Fill All The Fields!");
                return;
            }

            AlertDialog loading = functions.showLoading(this, "Signing in...");
            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailValue, passwordValue)
                    .addOnCompleteListener(task -> {
                        functions.hideLoading(loading);

                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Incorrect Password or Email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            Toast.makeText(getActivity(), "Login session failed. Please try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        functions.moveByType(this, user.getUid());
                    });
        });

        return pageui;
    }
}
