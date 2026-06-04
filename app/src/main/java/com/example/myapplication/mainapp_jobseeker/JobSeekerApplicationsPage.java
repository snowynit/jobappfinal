package com.example.myapplication.mainapp_jobseeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.example.myapplication.mainapp_jobseeker.Adapter.SavedJobsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JobSeekerApplicationsPage extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private TextView emptyTitle;
    private ProgressBar loadingIndicator;
    private Button filterAll;
    private Button filterApplied;
    private Button filterLiked;

    private final List<Job> allSavedJobs = new ArrayList<>();
    private final List<Job> filteredJobs = new ArrayList<>();
    private SavedJobsAdapter adapter;

    private String selectedFilter = "ALL";
    private int pendingLoads;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._js_nav_applicationpage, container, false);

        recyclerView = pageui.findViewById(R.id.appliedRecycler);
        emptyState = pageui.findViewById(R.id.emptyState);
        emptyTitle = pageui.findViewById(R.id.emptyTitle);
        loadingIndicator = pageui.findViewById(R.id.loadingIndicator);
        filterAll = pageui.findViewById(R.id.filterAll);
        filterApplied = pageui.findViewById(R.id.filterApplied);
        filterLiked = pageui.findViewById(R.id.filterLiked);
        Button goSearch = pageui.findViewById(R.id.goSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SavedJobsAdapter(filteredJobs, job ->
                functions.moveMain(this, new JobSeekerJobDetailsPage(), functions.jobToBundle(job)));
        recyclerView.setAdapter(adapter);

        functions.styleButton(filterAll, true);
        filterAll.setOnClickListener(v -> setFilter("ALL", filterAll, filterApplied, filterLiked));
        filterApplied.setOnClickListener(v -> setFilter("APPLIED", filterApplied, filterAll, filterLiked));
        filterLiked.setOnClickListener(v -> setFilter("LIKED", filterLiked, filterAll, filterApplied));
        goSearch.setOnClickListener(v -> functions.moveMain(this, new JobSeekerSearchPage(), null));

        loadSavedJobs();

        return pageui;
    }

    private void loadSavedJobs() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        allSavedJobs.clear();
        pendingLoads = 2;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("applications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = functions.mapApplicationDocument(document);
                        job.id = functions.getValue(document.getString("jobId"), job.id);
                        job.actionType = "Applied";
                        allSavedJobs.add(job);
                    }
                    finishLoad();
                })
                .addOnFailureListener(e -> finishLoad());

        db.collection("users").document(userId).collection("liked_jobs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = new Job();
                        job.id = functions.getValue(document.getString("jobId"), document.getId());
                        job.title = document.getString("title");
                        job.description = document.getString("description");
                        job.type = document.getString("type");
                        job.pay = document.getString("pay");
                        job.businessName = document.getString("businessName");
                        job.location = document.getString("location");
                        job.address = document.getString("address");
                        job.phone = document.getString("phone");
                        job.email = document.getString("email");
                        job.actionType = "Liked";
                        com.google.firebase.Timestamp ts = document.getTimestamp("likedAt");
                        job.savedDate = ts == null ? "Recently" : new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(ts.toDate());
                        allSavedJobs.add(job);
                    }
                    finishLoad();
                })
                .addOnFailureListener(e -> finishLoad());
    }

    private void finishLoad() {
        pendingLoads--;
        if (pendingLoads <= 0) {
            loadingIndicator.setVisibility(View.GONE);
            applyFilter();
        }
    }

    private void setFilter(String filter, Button selected, Button firstOther, Button secondOther) {
        selectedFilter = filter;
        functions.styleButton(selected, true);
        functions.styleButton(firstOther, false);
        functions.styleButton(secondOther, false);
        applyFilter();
    }

    private void applyFilter() {
        filteredJobs.clear();

        for (Job job : allSavedJobs) {
            boolean matches;
            if ("APPLIED".equals(selectedFilter)) {
                matches = "Applied".equalsIgnoreCase(job.actionType);
            } else if ("LIKED".equals(selectedFilter)) {
                matches = "Liked".equalsIgnoreCase(job.actionType);
            } else {
                matches = true;
            }

            if (matches) {
                filteredJobs.add(job);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredJobs.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            if ("APPLIED".equals(selectedFilter)) {
                emptyTitle.setText("You have not applied to any jobs yet");
            } else if ("LIKED".equals(selectedFilter)) {
                emptyTitle.setText("You have not liked any jobs yet");
            } else {
                emptyTitle.setText("You have no saved jobs yet");
            }
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }
}
