package com.example.myapplication.loginandsignup;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_business.MainAppBusiness;

import java.util.HashMap;
import java.util.Map;

public class BusinessRegisterPassword extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_bussines_register__third_b_password_, container, false);

        TextView showPassword = pageui.findViewById(R.id.ShowPassword);
        TextView alert = pageui.findViewById(R.id.alert);
        EditText password = pageui.findViewById(R.id.Password);
        EditText confirm = pageui.findViewById(R.id.ConfirmPassword);
        Button finish = pageui.findViewById(R.id.Finish);

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

        finish.setOnClickListener(view -> {
            if (!functions.passwordmatch(password, confirm)) {
                functions.alert(alert);
                return;
            }

            Bundle bundle = getArguments();
            if (bundle == null) {
                alert.setText("Missing business information. Please go back and try again.");
                alert.setVisibility(View.VISIBLE);
                return;
            }

            String accountType = functions.getValue(bundle.getString("AccountType"), "Business");
            String email = functions.getValue(bundle.getString("Email"), "").trim();
            Map<String, Object> data = new HashMap<>();
            data.put("Account Type", accountType);
            data.put("AccountType", accountType);
            data.put("Business Name", bundle.getString("Name"));
            data.put("Business Size", bundle.getString("Size"));
            data.put("Business Address", bundle.getString("Address"));
            data.put("Business Email", email);
            data.put("Business Phone", bundle.getString("Phone"));
            data.put("Business Pay", bundle.getString("Pay"));
            data.put("Hiring Needs", bundle.getStringArrayList("HiringNeeds"));
            data.put("Pay Rate", bundle.getStringArrayList("PayRate"));

            AlertDialog loading = functions.showLoading(this, "Creating business account...");
            functions.register(email, password.getText().toString(), data, task -> {
                functions.hideLoading(loading);
                if (task.isSuccessful()) {
                    startActivity(new Intent(requireContext(), MainAppBusiness.class));
                    requireActivity().finish();
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

        return pageui;
    }
}
