package com.example.myapplication.mainapp_jobseeker.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;

import java.util.List;

public class SavedJobsAdapter extends RecyclerView.Adapter<SavedJobsAdapter.SavedJobViewHolder> {

    public interface OnSavedJobClickListener {
        void onSavedJobClick(Job job);
    }

    private final List<Job> jobList;
    private final OnSavedJobClickListener listener;

    public SavedJobsAdapter(List<Job> jobList, OnSavedJobClickListener listener) {
        this.jobList = jobList;
        this.listener = listener;
    }

    static class SavedJobViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView status;
        TextView business;
        TextView pay;
        TextView date;

        SavedJobViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            status = itemView.findViewById(R.id.status);
            business = itemView.findViewById(R.id.business);
            pay = itemView.findViewById(R.id.pay);
            date = itemView.findViewById(R.id.date);
        }
    }

    @NonNull
    @Override
    public SavedJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout._item_applied_job, parent, false);
        return new SavedJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedJobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.title.setText(isBlank(job.title) ? "Open Position" : job.title);
        holder.business.setText(isBlank(job.businessName) ? "Business" : job.businessName);
        holder.pay.setText(isBlank(job.pay) ? "Pay not provided" : (job.pay.contains("₪") ? job.pay : "₪" + job.pay + "/hr"));

        if ("Liked".equalsIgnoreCase(job.actionType)) {
            holder.status.setText("Liked");
            holder.status.setBackgroundColor(Color.parseColor("#DBEAFE"));
            holder.status.setTextColor(Color.parseColor("#1D4ED8"));
            holder.date.setText("Liked on: " + safe(job.savedDate));
        } else {
            String status = isBlank(job.status) ? "Pending" : job.status;
            holder.status.setText(status);
            applyStatusStyle(holder.status, status);
            holder.date.setText("Applied on: " + safe(job.savedDate));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSavedJobClick(job);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    private void applyStatusStyle(TextView view, String status) {
        String lowered = status.toLowerCase();
        if (lowered.contains("accept")) {
            view.setBackgroundColor(Color.parseColor("#DCFCE7"));
            view.setTextColor(Color.parseColor("#166534"));
        } else if (lowered.contains("reject")) {
            view.setBackgroundColor(Color.parseColor("#FEE2E2"));
            view.setTextColor(Color.parseColor("#B91C1C"));
        } else {
            view.setBackgroundColor(Color.parseColor("#FEF3C7"));
            view.setTextColor(Color.parseColor("#92400E"));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return isBlank(value) ? "Recently" : value;
    }
}
