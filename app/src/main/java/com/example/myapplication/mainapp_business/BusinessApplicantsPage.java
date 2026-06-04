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
import com.example.myapplication.mainapp_business.Adapter.ApplicationReviewAdapter;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BusinessApplicantsPage extends Fragment {

    private final List<Job> allApplicants = new ArrayList<>();
    private final List<Job> filteredApplicants = new ArrayList<>();
    private ApplicationReviewAdapter adapter;
    private TextView emptyState;
    private Button filterAll;
    private Button filterPending;
    private Button filterAccepted;
    private Button filterRejected;
    private String selectedFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_applicants_page, container, false);

        emptyState = pageui.findViewById(R.id.emptyState);
        filterAll = pageui.findViewById(R.id.filterAll);
        filterPending = pageui.findViewById(R.id.filterPending);
        filterAccepted = pageui.findViewById(R.id.filterAccepted);
        filterRejected = pageui.findViewById(R.id.filterRejected);
        RecyclerView recyclerView = pageui.findViewById(R.id.applicantsRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ApplicationReviewAdapter(filteredApplicants, this::updateApplicationStatus);
        recyclerView.setAdapter(adapter);

        setFilter("ALL");
        filterAll.setOnClickListener(v -> setFilter("ALL"));
        filterPending.setOnClickListener(v -> setFilter("PENDING"));
        filterAccepted.setOnClickListener(v -> setFilter("ACCEPTED"));
        filterRejected.setOnClickListener(v -> setFilter("REJECTED"));

        loadApplicants();

        return pageui;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            loadApplicants();
        }
    }

    private void loadApplicants() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading applicants...");

        FirebaseFirestore.getInstance().collection("applications").get().addOnSuccessListener(queryDocumentSnapshots -> {
            functions.hideLoading(loading);
            allApplicants.clear();
            Set<String> seenIds = new HashSet<>();

            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                String businessId = functions.getValue(document.getString("businessId"), "");
                String legacyJobId = functions.getValue(document.getString("jobId"), "");
                if (!userId.equals(businessId) && !userId.equals(legacyJobId)) {
                    continue;
                }
                if (!seenIds.add(document.getId())) {
                    continue;
                }
                allApplicants.add(functions.mapApplicationDocument(document));
            }

            applyFilter();
        }).addOnFailureListener(e -> {
            functions.hideLoading(loading);
            allApplicants.clear();
            applyFilter();
        });
    }

    private void setFilter(String filter) {
        selectedFilter = filter;
        functions.styleButton(filterAll, "ALL".equals(filter));
        functions.styleButton(filterPending, "PENDING".equals(filter));
        functions.styleButton(filterAccepted, "ACCEPTED".equals(filter));
        functions.styleButton(filterRejected, "REJECTED".equals(filter));
        applyFilter();
    }

    private void applyFilter() {
        filteredApplicants.clear();
        for (Job job : allApplicants) {
            String status = functions.safeLower(job.status);
            boolean matches;
            if ("PENDING".equals(selectedFilter)) {
                matches = !status.contains("accept") && !status.contains("reject");
            } else if ("ACCEPTED".equals(selectedFilter)) {
                matches = status.contains("accept");
            } else if ("REJECTED".equals(selectedFilter)) {
                matches = status.contains("reject");
            } else {
                matches = true;
            }

            if (matches) {
                filteredApplicants.add(job);
            }
        }

        adapter.notifyDataSetChanged();
        emptyState.setVisibility(filteredApplicants.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateApplicationStatus(Job job, String status) {
        AlertDialog loading = functions.showLoading(this, "Updating application...");
        FirebaseFirestore.getInstance().collection("applications").document(job.id)
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    job.status = status;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Application " + status.toLowerCase() + ".", Toast.LENGTH_SHORT).show();
                    loadApplicants();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to update application.", Toast.LENGTH_SHORT).show();
                });
    }
}
