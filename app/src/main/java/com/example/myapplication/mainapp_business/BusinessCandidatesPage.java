package com.example.myapplication.mainapp_business;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_business.Adapter.CandidatesAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BusinessCandidatesPage extends Fragment {

    private final List<Candidate> all = new ArrayList<>();
    private final List<Candidate> filtered = new ArrayList<>();
    private CandidatesAdapter adapter;
    private TextView emptyState;
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._business_candidates_page, container, false);

        emptyState = pageui.findViewById(R.id.emptyCandidates);
        RecyclerView recycler = pageui.findViewById(R.id.candidatesRecycler);
        EditText search = pageui.findViewById(R.id.candidateSearch);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CandidatesAdapter(filtered, this::openOfferDialog);
        recycler.setAdapter(adapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                applyFilter();
            }
        });

        loadCandidates();
        return pageui;
    }

    private void loadCandidates() {
        AlertDialog loading = functions.showLoading(this, "Loading candidates...");
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(snapshots -> {
                    functions.hideLoading(loading);
                    all.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (isBusinessAccount(doc)) {
                            continue;
                        }
                        Candidate c = mapCandidate(doc);
                        if (c == null) {
                            continue;
                        }
                        all.add(c);
                    }
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    all.clear();
                    applyFilter();
                });
    }

    private boolean isBusinessAccount(DocumentSnapshot doc) {
        String type = functions.getValue(
                doc.getString("Account Type"),
                doc.getString("AccountType"),
                doc.getString("accountType")
        );
        if (type != null && type.toLowerCase(Locale.ROOT).contains("business")) {
            return true;
        }
        String bizName = doc.getString("Business Name");
        return bizName != null && !bizName.trim().isEmpty();
    }

    private Candidate mapCandidate(DocumentSnapshot doc) {
        String name = functions.getValue(doc.getString("FullName"), doc.getString("Name"), "");
        if (name.trim().isEmpty()) {
            return null;
        }
        Candidate c = new Candidate();
        c.id = doc.getId();
        c.name = name;
        c.bio = functions.getValue(doc.getString("Bio"), "");
        c.preferredType = functions.getValue(doc.getString("PreferredType"), "");
        c.preferredFields = functions.getValue(doc.getString("PreferredFields"), "");
        c.desiredPay = functions.getValue(doc.getString("DesiredPay"), "");
        c.profileImageUri = functions.getValue(doc.getString("ProfileImageUri"), "");
        c.resumeUri = functions.getValue(doc.getString("ResumeUri"), "");
        c.resumeName = functions.getValue(doc.getString("ResumeName"), "");
        c.email = functions.getValue(doc.getString("Email"), "");
        return c;
    }

    private void applyFilter() {
        filtered.clear();
        if (currentQuery.isEmpty()) {
            filtered.addAll(all);
        } else {
            for (Candidate c : all) {
                String haystack = (c.name + " " + c.preferredType + " " + c.preferredFields + " " + c.bio)
                        .toLowerCase(Locale.ROOT);
                if (haystack.contains(currentQuery)) {
                    filtered.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openOfferDialog(Candidate candidate) {
        OfferDialog.newInstance(candidate.id, candidate.name)
                .show(getChildFragmentManager(), "offer_dialog");
    }
}
