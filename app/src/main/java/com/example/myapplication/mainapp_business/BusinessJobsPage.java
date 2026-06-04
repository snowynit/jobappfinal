package com.example.myapplication.mainapp_business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_business.Adapter.BusinessJobsAdapter;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessJobsPage extends Fragment {

    private final List<Job> jobs = new ArrayList<>();
    private BusinessJobsAdapter adapter;
    private TextView emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_jobs_page, container, false);

        emptyState = pageui.findViewById(R.id.emptyState);
        Button createJobButton = pageui.findViewById(R.id.createJobButton);
        RecyclerView jobsRecycler = pageui.findViewById(R.id.jobsRecycler);

        jobsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BusinessJobsAdapter(jobs, new BusinessJobsAdapter.OnJobActionListener() {
            @Override
            public void onEdit(Job job) {
                functions.moveMain(BusinessJobsPage.this, new BusinessCreateJobPage(), functions.jobToBundle(job));
            }

            @Override
            public void onToggle(Job job) {
                toggleJobState(job);
            }
        });
        jobsRecycler.setAdapter(adapter);

        createJobButton.setOnClickListener(v -> functions.moveMain(this, new BusinessCreateJobPage(), null));

        loadJobs();

        return pageui;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            loadJobs();
        }
    }

    private void loadJobs() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading jobs...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("jobs").whereEqualTo("ownerId", userId).get().addOnSuccessListener(jobSnapshots ->
                db.collection("applications").get().addOnSuccessListener(applicationSnapshots -> {
                    functions.hideLoading(loading);
                    Map<String, Integer> counts = new HashMap<>();
                    for (DocumentSnapshot document : applicationSnapshots.getDocuments()) {
                        String businessId = functions.getValue(document.getString("businessId"), "");
                        if (!userId.equals(businessId)) {
                            continue;
                        }
                        String jobId = functions.getValue(document.getString("jobId"), "");
                        int currentCount = counts.containsKey(jobId) ? counts.get(jobId) : 0;
                        counts.put(jobId, currentCount + 1);
                    }

                    jobs.clear();
                    for (DocumentSnapshot document : jobSnapshots.getDocuments()) {
                        Job job = functions.mapJobDocument(document);
                        if (job == null) {
                            continue;
                        }
                        job.applicantsCount = counts.containsKey(job.id) ? counts.get(job.id) : 0;
                        jobs.add(job);
                    }

                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(jobs.isEmpty() ? View.VISIBLE : View.GONE);
                }).addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    jobs.clear();
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(View.VISIBLE);
                })
        ).addOnFailureListener(e -> {
            functions.hideLoading(loading);
            jobs.clear();
            adapter.notifyDataSetChanged();
            emptyState.setVisibility(View.VISIBLE);
        });
    }

    private void toggleJobState(Job job) {
        String nextState = functions.safeLower(job.jobState).contains("pause") ? "Active" : "Paused";
        AlertDialog loading = functions.showLoading(this, "Updating job state...");
        FirebaseFirestore.getInstance().collection("jobs").document(job.id)
                .update("jobState", nextState, "jobOpen", "Active".equals(nextState))
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    job.jobState = nextState;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Job " + nextState.toLowerCase() + ".", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to update job state.", Toast.LENGTH_SHORT).show();
                });
    }
}
