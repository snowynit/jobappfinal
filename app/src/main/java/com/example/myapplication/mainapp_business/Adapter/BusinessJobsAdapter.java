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

public class BusinessJobsAdapter extends RecyclerView.Adapter<BusinessJobsAdapter.BusinessJobViewHolder> {

    public interface OnJobActionListener {
        void onEdit(Job job);
        void onToggle(Job job);
    }

    private final List<Job> jobs;
    private final OnJobActionListener listener;

    public BusinessJobsAdapter(List<Job> jobs, OnJobActionListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    static class BusinessJobViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        TextView address;
        TextView applicantCount;
        TextView stateChip;
        Button editButton;
        Button toggleButton;

        BusinessJobViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            meta = itemView.findViewById(R.id.meta);
            address = itemView.findViewById(R.id.address);
            applicantCount = itemView.findViewById(R.id.applicantCount);
            stateChip = itemView.findViewById(R.id.stateChip);
            editButton = itemView.findViewById(R.id.editButton);
            toggleButton = itemView.findViewById(R.id.toggleButton);
        }
    }

    @NonNull
    @Override
    public BusinessJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout._item_business_job, parent, false);
        return new BusinessJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessJobViewHolder holder, int position) {
        Job job = jobs.get(position);

        holder.title.setText(functions.getValue(job.title, "Open Position"));
        String meta = (job.type == null || job.type.trim().isEmpty()) ? "Open role" : job.type.replace("_", " ").trim();
        if (job.pay != null && !job.pay.trim().isEmpty()) {
            String pay = job.pay.contains("₪") ? job.pay : "₪" + job.pay + "/hr";
            meta = meta + "  •  " + pay;
        }
        holder.meta.setText(meta);
        holder.address.setText(functions.getValue(job.address, job.location, "Address not provided"));
        holder.applicantCount.setText(job.applicantsCount + (job.applicantsCount == 1 ? " applicant" : " applicants"));

        String state = functions.getValue(job.jobState, "Active");
        holder.stateChip.setText(state);
        String stateLower = state.toLowerCase();
        if (stateLower.contains("pause") || stateLower.contains("close")) {
            holder.stateChip.setBackgroundColor(android.graphics.Color.parseColor("#E5E7EB"));
            holder.stateChip.setTextColor(android.graphics.Color.parseColor("#374151"));
        } else {
            holder.stateChip.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
            holder.stateChip.setTextColor(android.graphics.Color.parseColor("#166534"));
        }
        holder.toggleButton.setText(functions.safeLower(state).contains("pause") ? "Activate" : "Pause");

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(job);
            }
        });
        holder.toggleButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggle(job);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }
}
