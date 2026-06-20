package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.LocationPickerDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BusinessRegisterPage extends Fragment
        implements LocationPickerDialog.LocationSelectionListener {

    private TextView addressDisplay;
    private double businessLat = 0d;
    private double businessLng = 0d;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_bussiness_register__third_b_, container, false);
        Button full, half, temp, confirm, daily, hourly, setLocation;
        EditText name, email, phone, pay;
        functions PressedCheck = new functions();

        full = pageui.findViewById(R.id.fulltime);
        half = pageui.findViewById(R.id.halftime);
        temp = pageui.findViewById(R.id.temporary);
        confirm = pageui.findViewById(R.id.confirm);
        daily = pageui.findViewById(R.id.daily);
        hourly = pageui.findViewById(R.id.hourly);
        name = pageui.findViewById(R.id.businessName);
        addressDisplay = pageui.findViewById(R.id.businessAddress);
        setLocation = pageui.findViewById(R.id.setBusinessLocationButton);
        email = pageui.findViewById(R.id.businessEmail);
        phone = pageui.findViewById(R.id.businessPhone);
        pay = pageui.findViewById(R.id.payrate);

        // hiring needs - אפשר לבחור כמה
        PressedCheck.pressed(full);
        PressedCheck.pressed(half);
        PressedCheck.pressed(temp);

        // pay rate - רק אחד מהשניים
        final Button dailyBtn = daily;
        final Button hourlyBtn = hourly;
        functions.styleButton(dailyBtn, false);
        functions.styleButton(hourlyBtn, false);
        dailyBtn.setOnClickListener(v -> {
            boolean willSelect = !dailyBtn.isSelected();
            functions.styleButton(dailyBtn, willSelect);
            if (willSelect) functions.styleButton(hourlyBtn, false);
        });
        hourlyBtn.setOnClickListener(v -> {
            boolean willSelect = !hourlyBtn.isSelected();
            functions.styleButton(hourlyBtn, willSelect);
            if (willSelect) functions.styleButton(dailyBtn, false);
        });

        // פתיחת המפה של גוגל לבחירת מיקום העסק
        setLocation.setOnClickListener(v ->
                LocationPickerDialog.newInstance(
                        businessLat == 0d ? null : businessLat,
                        businessLng == 0d ? null : businessLng,
                        10d
                ).show(getChildFragmentManager(), "business_location_picker"));

        confirm.setOnClickListener(view -> {
            List<String> selectedneeds = new ArrayList<>();
            if (full.isSelected()) selectedneeds.add("Full-Time");
            if (half.isSelected()) selectedneeds.add("Half-Time");
            if (temp.isSelected()) selectedneeds.add("Temporary");

            List<String> selectedpay = new ArrayList<>();
            if (dailyBtn.isSelected()) selectedpay.add("Daily Pay");
            if (hourlyBtn.isSelected()) selectedpay.add("Hourly Pay");

            String nameText = name.getText().toString();
            String emailText = email.getText().toString();
            String phoneText = phone.getText().toString();
            String payText = pay.getText().toString();

            // בודק שהמיקום נבחר
            if (businessLat == 0d && businessLng == 0d) {
                Toast.makeText(getActivity(), "Please pick your business location on the map.", Toast.LENGTH_SHORT).show();
                return;
            }

            String addressText = String.format(Locale.US, "%.5f, %.5f", businessLat, businessLng);

            Bundle bundle = new Bundle();
            bundle.putString("AccountType", "Business");
            bundle.putString("Name", nameText);
            bundle.putString("Address", addressText);
            bundle.putDouble("Latitude", businessLat);
            bundle.putDouble("Longitude", businessLng);
            bundle.putString("Email", emailText);
            bundle.putString("Phone", phoneText);
            bundle.putString("Pay", payText);
            bundle.putStringArrayList("HiringNeeds", new ArrayList<>(selectedneeds));
            bundle.putStringArrayList("PayRate", new ArrayList<>(selectedpay));

            // ולידציה - בלי number of employees, עם מיקום במקום כתובת טקסט
            if (functions.Businessfilled(nameText, emailText, addressText,
                    "1", payText, new ArrayList<>(selectedneeds), new ArrayList<>(selectedpay))) {
                functions.move(this, new BusinessRegisterPassword(), bundle);
            } else {
                Toast.makeText(getActivity(), "Fill All The Information!", Toast.LENGTH_SHORT).show();
            }
        });

        return pageui;
    }

    // נקרא כשהמשתמש בחר נקודה במפה
    @Override
    public void onLocationSelected(double latitude, double longitude, double radiusKm) {
        businessLat = latitude;
        businessLng = longitude;
        if (addressDisplay != null) {
            addressDisplay.setText(String.format(Locale.US, "Selected: %.5f, %.5f", latitude, longitude));
        }
    }

    @Override
    public void onLocationCleared() {
        businessLat = 0d;
        businessLng = 0d;
        if (addressDisplay != null) {
            addressDisplay.setText("Location not selected");
        }
    }
}
