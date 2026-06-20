package com.example.myapplication.mainapp_jobseeker;

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
import com.example.myapplication.mainapp_jobseeker.Adapter.OffersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class JobSeekerOffersPage extends Fragment implements OffersAdapter.OnOfferAction {

    private final List<Offer> offers = new ArrayList<>();
    private OffersAdapter adapter;
    private TextView emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._js_offers_page, container, false);

        emptyState = pageui.findViewById(R.id.offersEmpty);
        RecyclerView recycler = pageui.findViewById(R.id.offersRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OffersAdapter(offers, this);
        recycler.setAdapter(adapter);

        loadOffers();
        return pageui;
    }

    // טוען את ההצעות שמיועדות למשתמש הנוכחי
    private void loadOffers() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        AlertDialog loading = functions.showLoading(this, "Loading offers...");

        FirebaseFirestore.getInstance().collection("offers")
                .whereEqualTo("seekerId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    functions.hideLoading(loading);
                    offers.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Offer o = new Offer();
                        o.id = doc.getId();
                        o.businessId = functions.getValue(doc.getString("businessId"), "");
                        o.businessName = functions.getValue(doc.getString("businessName"), "");
                        o.businessLogoUri = functions.getValue(doc.getString("businessLogoUri"), "");
                        o.businessEmail = functions.getValue(doc.getString("businessEmail"), "");
                        o.businessPhone = functions.getValue(doc.getString("businessPhone"), "");
                        o.businessAddress = functions.getValue(doc.getString("businessAddress"), "");
                        o.jobTitle = functions.getValue(doc.getString("jobTitle"), "");
                        o.message = functions.getValue(doc.getString("message"), "");
                        o.status = functions.getValue(doc.getString("status"), "pending");
                        offers.add(o);
                    }
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    offers.clear();
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(View.VISIBLE);
                });
    }

    // מעדכן את הסטטוס של ההצעה בפיירסטור (אושר / נדחה)
    private void updateStatus(Offer offer, String status) {
        AlertDialog loading = functions.showLoading(this, "Updating offer...");
        FirebaseFirestore.getInstance().collection("offers").document(offer.id)
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    functions.hideLoading(loading);
                    offer.status = status;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Offer " + status + ".", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    functions.hideLoading(loading);
                    Toast.makeText(getContext(), "Failed to update offer.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAccept(Offer offer) {
        updateStatus(offer, "accepted");
    }

    @Override
    public void onDecline(Offer offer) {
        updateStatus(offer, "declined");
    }
}
