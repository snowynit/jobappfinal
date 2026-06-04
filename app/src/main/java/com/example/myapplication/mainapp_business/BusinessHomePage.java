package com.example.myapplication.mainapp_business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.myapplication.mainapp_business.Adapter.ApplicationReviewAdapter;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BusinessHomePage extends Fragment {

    private final List<Job> recentApplications = new ArrayList<>();
    private ApplicationReviewAdapter adapter;

    private TextView businessNameText;
    private TextView statActiveJobs;
    private TextView statApplicants;
    private TextView statPending;
    private TextView statAccepted;
    private TextView emptyRecentState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._homes_homepage_bussiness, container, false);

        businessNameText = pageui.findViewById(R.id.txtBusinessName);
        statActiveJobs = pageui.findViewById(R.id.statActiveJobs);
        statApplicants = pageui.findViewById(R.id.statApplicants);
        statPending = pageui.findViewById(R.id.statPending);
        statAccepted = pageui.findViewById(R.id.statAccepted);
        emptyRecentState = pageui.findViewById(R.id.emptyRecentState);
        View createJobButton = pageui.findViewById(R.id.createJobButton);
        View reviewApplicantsButton = pageui.findViewById(R.id.reviewApplicantsButton);
        View editProfileButton = pageui.findViewById(R.id.editProfileButton);
        View manageJobsButton = pageui.findViewById(R.id.manageJobsButton);
        RecyclerView recyclerView = pageui.findViewById(R.id.businessApplicationsRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new ApplicationReviewAdapter(recentApplications, this::updateApplicationStatus);
        recyclerView.setAdapter(adapter);

        createJobButton.setOnClickListener(v -> functions.moveMain(this, new BusinessCreateJobPage(), null));
        reviewApplicantsButton.setOnClickListener(v -> functions.moveMain(this, new BusinessApplicantsPage(), null));
        editProfileButton.setOnClickListener(v -> functions.moveMain(this, new BusinessProfilePage(), null));
        manageJobsButton.setOnClickListener(v -> functions.moveMain(this, new BusinessJobsPage(), null));

        loadDashboard();

        return pageui;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            loadDashboard();
        }
    }

    private void loadDashboard() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading dashboard...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(document ->
                businessNameText.setText(functions.getValue(document.getString("Business Name"), document.getString("Name"), "Business Dashboard")));

        db.collection("jobs")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(jobSnapshots -> {
                    int activeCount = 0;
                    for (DocumentSnapshot document : jobSnapshots.getDocuments()) {
                        Job job = functions.mapJobDocument(document);
                        if (job != null) {
                            String state = functions.safeLower(job.jobState);
                            if (state.isEmpty() || (!state.contains("pause") && !state.contains("close"))) {
                                activeCount++;
                            }
                        }
                    }
                    statActiveJobs.setText(String.valueOf(activeCount));
                })
                .addOnFailureListener(e -> statActiveJobs.setText("0"));

        db.collection("applications").get().addOnSuccessListener(applicationSnapshots -> {
            recentApplications.clear();
            int totalApplicants = 0;
            int pendingCount = 0;
            int acceptedCount = 0;
            Set<String> seenIds = new HashSet<>();

            for (DocumentSnapshot document : applicationSnapshots.getDocuments()) {
                String businessId = functions.getValue(document.getString("businessId"), "");
                String legacyJobId = functions.getValue(document.getString("jobId"), "");
                if (!userId.equals(businessId) && !userId.equals(legacyJobId)) {
                    continue;
                }
                if (!seenIds.add(document.getId())) {
                    continue;
                }

                Job job = functions.mapApplicationDocument(document);
                totalApplicants++;

                String lowered = functions.safeLower(job.status);
                if (lowered.contains("accept")) {
                    acceptedCount++;
                } else if (!lowered.contains("reject")) {
                    pendingCount++;
                }

                if (recentApplications.size() < 3) {
                    recentApplications.add(job);
                }
            }

                    statApplicants.setText(String.valueOf(totalApplicants));
                    statPending.setText(String.valueOf(pendingCount));
                    statAccepted.setText(String.valueOf(acceptedCount));
                    emptyRecentState.setVisibility(recentApplications.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.notifyDataSetChanged();
                    functions.hideLoading(loading);
                }).addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    statApplicants.setText("0");
                    statPending.setText("0");
                    statAccepted.setText("0");
            recentApplications.clear();
            adapter.notifyDataSetChanged();
            emptyRecentState.setVisibility(View.VISIBLE);
        });
    }

    private void updateApplicationStatus(Job job, String status) {
        AlertDialog loading = functions.showLoading(this, "Updating application...");
        FirebaseFirestore.getInstance().collection("applications").document(job.id)
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    job.status = status;
                    adapter.notifyDataSetChanged();
                    loadDashboard();
                    Toast.makeText(getContext(), "Application " + status.toLowerCase() + ".", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to update application.", Toast.LENGTH_SHORT).show();
                });
    }
}
