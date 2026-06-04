package com.example.myapplication.mainapp_jobseeker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.Offer;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.VH> {

    public interface OnOfferAction {
        void onAccept(Offer offer);
        void onDecline(Offer offer);
    }

    private final List<Offer> data;
    private final OnOfferAction listener;

    public OffersAdapter(List<Offer> data, OnOfferAction listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._item_offer, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Offer o = data.get(position);
        holder.businessName.setText(functions.getValue(o.businessName, "Business"));
        holder.jobTitle.setText(functions.getValue(o.jobTitle, "Hiring offer"));
        holder.message.setText(functions.getValue(o.message, "No additional message."));

        String contact;
        if (!o.businessEmail.isEmpty() && !o.businessPhone.isEmpty()) {
            contact = "Contact: " + o.businessEmail + " · " + o.businessPhone;
        } else if (!o.businessEmail.isEmpty()) {
            contact = "Contact: " + o.businessEmail;
        } else if (!o.businessPhone.isEmpty()) {
            contact = "Contact: " + o.businessPhone;
        } else {
            contact = "Contact details will appear after acceptance.";
        }
        holder.contact.setText(contact);

        functions.loadImageUri(holder.logo, o.businessLogoUri, R.drawable.pfp);

        String status = functions.getValue(o.status, "pending");
        String lowered = status.toLowerCase();
        if (lowered.contains("accept")) {
            holder.status.setText("Accepted");
            holder.actions.setVisibility(View.GONE);
        } else if (lowered.contains("declin") || lowered.contains("reject")) {
            holder.status.setText("Declined");
            holder.actions.setVisibility(View.GONE);
        } else {
            holder.status.setText("Pending");
            holder.actions.setVisibility(View.VISIBLE);
        }

        holder.accept.setOnClickListener(v -> listener.onAccept(o));
        holder.decline.setOnClickListener(v -> listener.onDecline(o));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView logo;
        final TextView businessName, jobTitle, message, contact, status;
        final LinearLayout actions;
        final MaterialButton accept, decline;

        VH(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.offerBusinessLogo);
            businessName = itemView.findViewById(R.id.offerBusinessName);
            jobTitle = itemView.findViewById(R.id.offerJobTitle);
            message = itemView.findViewById(R.id.offerMessage);
            contact = itemView.findViewById(R.id.offerContact);
            status = itemView.findViewById(R.id.offerStatusBadge);
            actions = itemView.findViewById(R.id.offerActions);
            accept = itemView.findViewById(R.id.offerAcceptButton);
            decline = itemView.findViewById(R.id.offerDeclineButton);
        }
    }
}
