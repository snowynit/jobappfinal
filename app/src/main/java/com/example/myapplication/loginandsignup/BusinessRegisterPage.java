package com.example.myapplication.loginandsignup;

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

import java.util.ArrayList;
import java.util.List;

public class BusinessRegisterPage extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//      get the current page ui to show in the frame layout
        View pageui = inflater.inflate(R.layout._signup_bussiness_register__third_b_, container, false);
        Button full, half, temp, confirm, daily, hourly;
        EditText name, size, address, email, phone, pay;
        functions PressedCheck = new functions();

        full = pageui.findViewById(R.id.fulltime);
        half = pageui.findViewById(R.id.halftime);
        temp = pageui.findViewById(R.id.temporary);
        confirm = pageui.findViewById(R.id.confirm);
        daily = pageui.findViewById(R.id.daily);
        hourly = pageui.findViewById(R.id.hourly);
        name = pageui.findViewById(R.id.businessName);
        size = pageui.findViewById(R.id.businessSize);
        address = pageui.findViewById(R.id.businessAddress);
        email = pageui.findViewById(R.id.businessEmail);
        phone = pageui.findViewById(R.id.businessPhone);
        pay = pageui.findViewById(R.id.payrate);

        //        needs
        PressedCheck.pressed(full);
        PressedCheck.pressed(half);
        PressedCheck.pressed(temp);

        //        pay
        PressedCheck.pressed(daily);
        PressedCheck.pressed(hourly);

        //go to next fragment
        confirm.setOnClickListener(view -> {
            //needs list
            List<String> selectedneeds = new ArrayList<>();
            if (full.isSelected()) selectedneeds.add("Full-Time");
            if (half.isSelected()) selectedneeds.add("Half-Time");
            if (temp.isSelected()) selectedneeds.add("Temporary");

            //pay list
            List<String> selectedpay = new ArrayList<>();
            if (daily.isSelected()) selectedpay.add("Daily Pay");
            if (hourly.isSelected()) selectedpay.add("Hourly Pay");

            String nameText = name.getText().toString();
            String sizeText = size.getText().toString();
            String addressText = address.getText().toString();
            String emailText = email.getText().toString();
            String phoneText = phone.getText().toString();
            String payText = pay.getText().toString();

            // sending to next fragment
            Bundle bundle = new Bundle();
            bundle.putString("AccountType", "Business");
            bundle.putString("Name", nameText);
            bundle.putString("Size", sizeText);
            bundle.putString("Address", addressText);
            bundle.putString("Email", emailText);
            bundle.putString("Phone", phoneText);
            bundle.putString("Pay", payText);
            bundle.putStringArrayList("HiringNeeds", new ArrayList<>(selectedneeds));
            bundle.putStringArrayList("PayRate", new ArrayList<>(selectedpay));
            if (functions.Businessfilled(nameText, emailText, addressText,
                    sizeText, payText, new ArrayList<>(selectedneeds), new ArrayList<>(selectedpay))) {
                functions.move(this, new BusinessRegisterPassword(), bundle);
            }
            else {
                Toast.makeText(getActivity(), "Fill All The Information!", Toast.LENGTH_SHORT).show();
            }
        });

        return pageui;
    }
}
