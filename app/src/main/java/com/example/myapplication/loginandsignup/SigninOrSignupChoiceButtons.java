package com.example.myapplication.loginandsignup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;

public class SigninOrSignupChoiceButtons extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_signupandsignin_buttons_choice__first__, container, false);

        Button Signup, Signin;

        Signup = pageui.findViewById(R.id.signup);
        Signin = pageui.findViewById(R.id.signin);

        Signup.setOnClickListener(view -> {
            getParentFragmentManager().beginTransaction().replace(R.id.baseframe, new HireOrSeek()).addToBackStack(null).commit();
        });

        Signin.setOnClickListener(view -> {
            getParentFragmentManager().beginTransaction().replace(R.id.baseframe, new LoginPage()).addToBackStack(null).commit();
        });
        return pageui;
    }
}

