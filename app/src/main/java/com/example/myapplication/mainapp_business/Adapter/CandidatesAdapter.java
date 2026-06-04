package com.example.myapplication.mainapp_business.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_business.Candidate;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CandidatesAdapter extends RecyclerView.Adapter<CandidatesAdapter.VH> {

    public interface OnOfferListener {
        void onSendOffer(Candidate candidate);
    }

    private final List<Candidate> data;
    private final OnOfferListener listener;

    public CandidatesAdapter(List<Candidate> data, OnOfferListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._item_candidate, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Candidate c = data.get(position);
        holder.name.setText(functions.getValue(c.name, "Candidate"));
        holder.bio.setText(functions.getValue(c.bio, "No bio yet."));
        holder.type.setText(functions.getValue(c.preferredType, "Any type"));
        holder.fields.setText(functions.getValue(c.preferredFields, "No fields"));
        holder.pay.setText(c.desiredPay.isEmpty() ? "Open to offers" : "Desired pay: " + c.desiredPay);
        functions.loadImageUri(holder.avatar, c.profileImageUri, R.drawable.pfp);
        holder.sendOffer.setOnClickListener(v -> listener.onSendOffer(c));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name, bio, type, fields, pay;
        final MaterialButton sendOffer;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.candidateAvatar);
            name = itemView.findViewById(R.id.candidateName);
            bio = itemView.findViewById(R.id.candidateBio);
            type = itemView.findViewById(R.id.candidateType);
            fields = itemView.findViewById(R.id.candidateFields);
            pay = itemView.findViewById(R.id.candidatePay);
            sendOffer = itemView.findViewById(R.id.sendOfferButton);
        }
    }
}
