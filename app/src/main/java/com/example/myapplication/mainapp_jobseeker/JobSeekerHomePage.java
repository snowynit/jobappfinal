package com.example.myapplication.mainapp_jobseeker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class JobSeekerHomePage extends Fragment {

    private final List<Job> recommendedJobs = new ArrayList<>();
    private JobAdapter adapter;

    private String preferredType = "";
    private String preferredFields = "";
    private String desiredPay = "";

    private TextView nameText;
    private TextView bioText;
    private TextView preferredTypeText;
    private TextView preferredFieldsText;
    private TextView emptyRecommendations;
    private ImageView profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._homes_homepage_jobseeker, container, false);

        nameText = pageui.findViewById(R.id.name);
        bioText = pageui.findViewById(R.id.bio);
        preferredTypeText = pageui.findViewById(R.id.preferredType);
        preferredFieldsText = pageui.findViewById(R.id.preferredFields);
        emptyRecommendations = pageui.findViewById(R.id.emptyRecommendations);
        profileImage = pageui.findViewById(R.id.profileImage);
        RecyclerView recommendedRecycler = pageui.findViewById(R.id.recommendedRecycler);
        View openSearchCard = pageui.findViewById(R.id.openSearchCard);
        View openOffersCard = pageui.findViewById(R.id.openOffersCard);

        recommendedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new JobAdapter(recommendedJobs, job ->
                functions.moveMain(this, new JobSeekerJobDetailsPage(), functions.jobToBundle(job)));
        recommendedRecycler.setAdapter(adapter);

        openSearchCard.setOnClickListener(v -> functions.moveMain(this, new JobSeekerSearchPage(), null));
        openOffersCard.setOnClickListener(v -> functions.moveMain(this, new JobSeekerOffersPage(), null));

        loadProfileAndRecommendations();

        return pageui;
    }

    private void loadProfileAndRecommendations() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(info -> {
            if (info.exists()) {
                nameText.setText(functions.getValue(info.getString("FullName"), "Job Seeker"));
                bioText.setText(functions.getValue(
                        info.getString("Bio"),
                        "Build out your profile to get cleaner recommendations and stronger matches."
                ));

                preferredType = functions.getValue(info.getString("PreferredType"), "");
                preferredFields = functions.getValue(info.getString("PreferredFields"), "");
                desiredPay = functions.getValue(info.getString("DesiredPay"), "");

                preferredTypeText.setText(functions.getValue(preferredType, "Any work type"));
                preferredFieldsText.setText(functions.getValue(preferredFields, "No fields yet"));
                functions.loadImageUri(profileImage, info.getString("ProfileImageUri"), R.drawable.pfp);
            }

            loadRecommendedJobs();
        });
    }

    private void loadRecommendedJobs() {
        FirebaseFirestore.getInstance().collection("jobs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recommendedJobs.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Job job = functions.mapJobDocument(document);
                        if (job != null) {
                            recommendedJobs.add(job);
                        }
                    }

                    functions.sortRecommendedJobs(recommendedJobs, preferredType, preferredFields, desiredPay);
                    if (recommendedJobs.size() > 6) {
                        recommendedJobs.subList(6, recommendedJobs.size()).clear();
                    }

                    adapter.notifyDataSetChanged();
                    emptyRecommendations.setVisibility(recommendedJobs.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> emptyRecommendations.setVisibility(View.VISIBLE));
    }
}
