package com.example.myapplication.mainapp_jobseeker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    public interface OnJobClickListener {
        void onJobClick(Job job);
    }

    private final List<Job> jobList;
    private final OnJobClickListener listener;

    public JobAdapter(List<Job> jobList, OnJobClickListener listener) {
        this.jobList = jobList;
        this.listener = listener;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView businessName;
        TextView title;
        TextView type;
        TextView pay;
        TextView description;
        TextView location;

        public JobViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            type = itemView.findViewById(R.id.type);
            pay = itemView.findViewById(R.id.pay);
            description = itemView.findViewById(R.id.description);
            businessName = itemView.findViewById(R.id.businessName);
            location = itemView.findViewById(R.id.location);
        }
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout._item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        holder.businessName.setText(isBlank(job.businessName) ? "Business" : job.businessName);
        holder.title.setText(isBlank(job.title) ? "Open Position" : job.title);
        holder.type.setText(isBlank(job.type) ? "Open role" : job.type.replace("_", " ").trim());
        holder.description.setText(isBlank(job.description) ? "No description available yet." : job.description);

        if (isBlank(job.location)) {
            holder.location.setVisibility(View.GONE);
        } else {
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setText(job.location);
        }

        if (isBlank(job.pay)) {
            holder.pay.setVisibility(View.GONE);
        } else {
            holder.pay.setVisibility(View.VISIBLE);
            holder.pay.setText(job.pay.contains("₪") ? job.pay : "₪" + job.pay + "/hr");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJobClick(job);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
