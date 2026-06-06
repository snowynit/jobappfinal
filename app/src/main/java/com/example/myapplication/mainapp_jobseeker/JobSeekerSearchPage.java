package com.example.myapplication.mainapp_jobseeker;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.example.myapplication.mainapp_jobseeker.Adapter.JobAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class JobSeekerSearchPage extends Fragment implements LocationPickerDialog.LocationSelectionListener {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private TextView locationSummary;
    private ProgressBar loadingIndicator;
    private Button fulltime;
    private Button halftime;
    private Button temp;
    private Button changeLocationButton;

    private final List<Job> allJobs = new ArrayList<>();
    private final List<Job> filteredJobs = new ArrayList<>();

    private JobAdapter adapter;
    private String selectedType = "";
    private Double filterLat = null;
    private Double filterLng = null;
    private double filterRadius = 0d;

    private final Handler handler = new Handler();
    private Runnable runnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._js_nav_searchpage, container, false);

//      assign things
        searchInput = pageui.findViewById(R.id.searchInput);
        recyclerView = pageui.findViewById(R.id.searchRecycler);
        emptyText = pageui.findViewById(R.id.emptyText);
        locationSummary = pageui.findViewById(R.id.locationSummary);
        loadingIndicator = pageui.findViewById(R.id.loadingIndicator);
        fulltime = pageui.findViewById(R.id.fulltime);
        halftime = pageui.findViewById(R.id.parttime);
        temp = pageui.findViewById(R.id.temp);
        changeLocationButton = pageui.findViewById(R.id.changeLocationButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new JobAdapter(filteredJobs, job ->
                functions.moveMain(this, new JobSeekerJobDetailsPage(), functions.jobToBundle(job)));
        recyclerView.setAdapter(adapter);

        functions.styleButton(fulltime, false);
        functions.styleButton(halftime, false);
        functions.styleButton(temp, false);

        fulltime.setOnClickListener(v -> setSelectedType("FULL_TIME", fulltime, halftime, temp));
        halftime.setOnClickListener(v -> setSelectedType("PART_TIME", halftime, fulltime, temp));
        temp.setOnClickListener(v -> setSelectedType("TEMPORARY", temp, fulltime, halftime));
        changeLocationButton.setOnClickListener(v ->
                LocationPickerDialog
                        .newInstance(filterLat, filterLng, filterRadius)
                        .show(getChildFragmentManager(), "location_picker")
        );

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
                runnable = JobSeekerSearchPage.this::filterJobs;
                handler.postDelayed(runnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadSavedLocation();
        loadJobsFromDatabase();
        return pageui;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void loadJobsFromDatabase() {
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        FirebaseFirestore.getInstance().collection("jobs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allJobs.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Job job = functions.mapJobDocument(document);
                        if (job != null) {
                            allJobs.add(job);
                        }
                    }

                    loadingIndicator.setVisibility(View.GONE);
                    filterJobs();
                })
                .addOnFailureListener(e -> {
                    allJobs.clear();
                    filteredJobs.clear();
                    adapter.notifyDataSetChanged();
                    loadingIndicator.setVisibility(View.GONE);
                    emptyText.setText("Failed to load jobs");
                    emptyText.setVisibility(View.VISIBLE);
                });
    }

    private void loadSavedLocation() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    Boolean enabled = document.getBoolean("UseLocation");
                    Double latitude = document.getDouble("PreferredLatitude");
                    Double longitude = document.getDouble("PreferredLongitude");
                    Double radius = document.getDouble("PreferredRadiusKm");

                    if (!Boolean.TRUE.equals(enabled) || latitude == null || longitude == null) {
                        return;
                    }

                    filterLat = latitude;
                    filterLng = longitude;
                    filterRadius = radius != null && radius > 0d ? radius : 10d;
                    locationSummary.setText("Saved area: " + Math.round(filterRadius) + " km radius");
                    filterJobs();
                });
    }

    private void filterJobs() {
        filteredJobs.clear();

        String query = functions.safeLower(searchInput.getText().toString());

        for (Job job : allJobs) {
            String title = functions.safeLower(job.title);
            String description = functions.safeLower(job.description);
            String businessName = functions.safeLower(job.businessName);
            String type = functions.safeLower(job.type).replace("-", "_").replace(" ", "_");
            String jobState = functions.safeLower(job.jobState);
            String selectedTypeNormalized = functions.safeLower(selectedType).replace("-", "_").replace(" ", "_");

            boolean matchesText = title.contains(query)
                    || description.contains(query)
                    || businessName.contains(query);

            boolean matchesType = selectedTypeNormalized.isEmpty() || type.contains(selectedTypeNormalized);
            boolean isVisible = jobState.isEmpty() || (!jobState.contains("pause") && !jobState.contains("close"));
            boolean matchesLocation = matchesSelectedLocation(job);

            if (matchesText && matchesType && isVisible && matchesLocation) {
                filteredJobs.add(job);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredJobs.isEmpty()) {
            emptyText.setText("No jobs found");
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private void setSelectedType(String type, Button selectedButton, Button firstOther, Button secondOther) {
        if (selectedType.equals(type)) {
            selectedType = "";
            functions.styleButton(selectedButton, false);
        } else {
            selectedType = type;
            functions.styleButton(selectedButton, true);
            functions.styleButton(firstOther, false);
            functions.styleButton(secondOther, false);
        }

        filterJobs();
    }

    @Override
    public void onLocationSelected(double latitude, double longitude, double radiusKm) {
        filterLat = latitude;
        filterLng = longitude;
        filterRadius = radiusKm;
        locationSummary.setText("Area: " + Math.round(radiusKm) + " km radius");
        filterJobs();
    }

    @Override
    public void onLocationCleared() {
        filterLat = null;
        filterLng = null;
        filterRadius = 0d;
        locationSummary.setText("Location: anywhere");
        filterJobs();
    }

    private boolean matchesSelectedLocation(Job job) {
        if (filterLat == null || filterLng == null || filterRadius <= 0d) {
            return true;
        }
        return functions.jobInRadius(job, filterLat, filterLng, filterRadius);
    }
}
