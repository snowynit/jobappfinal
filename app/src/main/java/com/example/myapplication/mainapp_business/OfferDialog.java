package com.example.myapplication.mainapp_business;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OfferDialog extends DialogFragment {

    private static final String ARG_SEEKER_ID = "seekerId";
    private static final String ARG_SEEKER_NAME = "seekerName";

    public static OfferDialog newInstance(String seekerId, String seekerName) {
        OfferDialog dialog = new OfferDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SEEKER_ID, seekerId);
        args.putString(ARG_SEEKER_NAME, seekerName);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public int getTheme() {
        return android.R.style.Theme_Translucent_NoTitleBar;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout._dialog_offer, container, false);

        String seekerName = getArguments() == null ? "" : functions.getValue(getArguments().getString(ARG_SEEKER_NAME), "candidate");
        String seekerId = getArguments() == null ? "" : functions.getValue(getArguments().getString(ARG_SEEKER_ID), "");

        TextView candidateLabel = root.findViewById(R.id.offerCandidateName);
        EditText jobTitleInput = root.findViewById(R.id.offerJobTitleInput);
        EditText messageInput = root.findViewById(R.id.offerMessageInput);
        Button cancel = root.findViewById(R.id.offerCancelButton);
        MaterialButton send = root.findViewById(R.id.offerSendButton);

        candidateLabel.setText("To: " + seekerName);
        cancel.setOnClickListener(v -> dismiss());
        send.setOnClickListener(v -> sendOffer(
                seekerId,
                seekerName,
                jobTitleInput.getText().toString().trim(),
                messageInput.getText().toString().trim()
        ));

        return root;
    }

    private void sendOffer(String seekerId, String seekerName, String jobTitle, String message) {
        if (seekerId.isEmpty()) {
            Toast.makeText(getContext(), "Missing candidate ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (jobTitle.isEmpty() && message.isEmpty()) {
            Toast.makeText(getContext(), "Add a role or a short message first.", Toast.LENGTH_SHORT).show();
            return;
        }
        String businessId = FirebaseAuth.getInstance().getUid();
        if (businessId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(businessId).get().addOnSuccessListener(businessDoc -> {
            Map<String, Object> offer = new HashMap<>();
            offer.put("businessId", businessId);
            offer.put("businessName", functions.getValue(
                    businessDoc.getString("Business Name"),
                    businessDoc.getString("FullName"),
                    "Business"));
            offer.put("businessLogoUri", functions.getValue(businessDoc.getString("Business Logo Uri"), ""));
            offer.put("businessEmail", functions.getValue(businessDoc.getString("Business Email"), ""));
            offer.put("businessPhone", functions.getValue(businessDoc.getString("Business Phone"), ""));
            offer.put("businessAddress", functions.getValue(businessDoc.getString("Business Address"), ""));
            offer.put("jobTitle", jobTitle);
            offer.put("message", message);
            offer.put("seekerId", seekerId);
            offer.put("seekerName", seekerName);
            offer.put("status", "pending");
            offer.put("createdAt", FieldValue.serverTimestamp());

            db.collection("offers").add(offer)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Offer sent to " + seekerName + ".", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to send offer.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load business info.", Toast.LENGTH_SHORT).show());
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
