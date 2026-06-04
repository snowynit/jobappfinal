package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class HireOrSeek extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_recruit_or_seek__second_a_, container, false);

        View recruit = pageui.findViewById(R.id.Employee);
        View seek = pageui.findViewById(R.id.Employer);

        recruit.setOnClickListener(view ->
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.baseframe, new BasicPersonalRegisterPage())
                        .addToBackStack(null)
                        .commit());

        seek.setOnClickListener(view ->
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.baseframe, new BusinessRegisterPage())
                        .addToBackStack(null)
                        .commit());

        return pageui;
    }
}
