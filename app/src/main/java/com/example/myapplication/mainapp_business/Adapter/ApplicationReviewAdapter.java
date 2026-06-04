package com.example.myapplication.mainapp_business.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;

import java.util.List;

public class ApplicationReviewAdapter extends RecyclerView.Adapter<ApplicationReviewAdapter.ReviewViewHolder> {

    public interface OnDecisionListener {
        void onDecision(Job job, String status);
    }

    private final List<Job> jobs;
    private final OnDecisionListener listener;

    public ApplicationReviewAdapter(List<Job> jobs, OnDecisionListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView applicantName;
        TextView jobTitle;
        TextView resumeName;
        TextView metaText;
        TextView resumePreview;
        TextView status;
        Button rejectButton;
        Button acceptButton;

        ReviewViewHolder(View itemView) {
            super(itemView);
            applicantName = itemView.findViewById(R.id.applicantName);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            resumeName = itemView.findViewById(R.id.resumeName);
            metaText = itemView.findViewById(R.id.metaText);
            resumePreview = itemView.findViewById(R.id.resumePreview);
            status = itemView.findViewById(R.id.status);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout._item_business_application, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.applicantName.setText(job.applicantName == null || job.applicantName.isEmpty() ? "Applicant" : job.applicantName);
        holder.jobTitle.setText(job.title == null || job.title.isEmpty() ? "Open Position" : job.title);
        holder.resumeName.setText(job.resumeName == null || job.resumeName.isEmpty() ? "Resume attached" : job.resumeName);
        String sourceLabel = "Resume";
        if ("builder".equalsIgnoreCase(job.resumeSource)) {
            sourceLabel = "Built in app";
        } else if ("upload".equalsIgnoreCase(job.resumeSource)) {
            sourceLabel = "Uploaded file";
        }
        String meta = functions.getValue(job.savedDate, "Recently") + "  •  " + sourceLabel;
        holder.metaText.setText(meta);

        if (job.resumeText == null || job.resumeText.trim().isEmpty()) {
            holder.resumePreview.setVisibility(View.GONE);
        } else {
            holder.resumePreview.setVisibility(View.VISIBLE);
            String preview = job.resumeText.trim();
            if (preview.length() > 140) {
                preview = preview.substring(0, 140).trim() + "...";
            }
            holder.resumePreview.setText(preview);
        }

        String status = functions.getValue(job.status, "Pending");
        holder.status.setText(status);
        String statusLower = status.toLowerCase();
        if (statusLower.contains("accept")) {
            holder.status.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
            holder.status.setTextColor(android.graphics.Color.parseColor("#166534"));
        } else if (statusLower.contains("reject")) {
            holder.status.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"));
            holder.status.setTextColor(android.graphics.Color.parseColor("#B91C1C"));
        } else {
            holder.status.setBackgroundColor(android.graphics.Color.parseColor("#FEF3C7"));
            holder.status.setTextColor(android.graphics.Color.parseColor("#92400E"));
        }

        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecision(job, "Accepted");
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecision(job, "Rejected");
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }
}
