package com.example.myapplication;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.loginandsignup.BasicPersonalRegisterPage;
import com.example.myapplication.loginandsignup.BusinessRegisterPage;
import com.example.myapplication.loginandsignup.BusinessRegisterPassword;
import com.example.myapplication.loginandsignup.ForgotPassword;
import com.example.myapplication.loginandsignup.HireOrSeek;
import com.example.myapplication.loginandsignup.LoginPage;
import com.example.myapplication.mainapp_business.MainAppBusiness;
import com.example.myapplication.mainapp_jobseeker.MainAppJobSeeker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class important {
//    -----------------------------
public class BasicPersonalRegisterPage extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View pageui = inflater.inflate(R.layout._signup_personal_register__third_a_, container, false);

        TextView showPassword = pageui.findViewById(R.id.ShowPassword);
        TextView alert = pageui.findViewById(R.id.alert);
        TextView needed = pageui.findViewById(R.id.information);
        EditText password = pageui.findViewById(R.id.Password);
        EditText confirm = pageui.findViewById(R.id.ConfirmPassword);
        EditText name = pageui.findViewById(R.id.Name);
        EditText email = pageui.findViewById(R.id.Email);
        EditText phone = pageui.findViewById(R.id.PhoneNumber);
        Button next = pageui.findViewById(R.id.next);
        ImageView info = pageui.findViewById(R.id.info);

        showPassword.setPaintFlags(showPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        showPassword.setOnClickListener(view -> {
            boolean isHidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (isHidden) {
                functions.show(password, confirm);
                showPassword.setText("Hide Password");
            } else {
                functions.hide(password, confirm);
                showPassword.setText("Show Password");
            }
        });

        next.setOnClickListener(view -> {
            if (!functions.passwordmatch(password, confirm)) {
                functions.alert(alert);
                return;
            }
            alert.setVisibility(View.GONE);

            if (!functions.Personalfilled(name, email)) {
                Toast.makeText(getActivity(), "Fill All The Information!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("FullName", name.getText().toString().trim());
            data.put("PhoneNumber", phone.getText().toString().trim());
            data.put("Account Type", "Job Seeker");
            data.put("AccountType", "Job Seeker");
            data.put("Email", email.getText().toString().trim());

            AlertDialog loading = functions.showLoading(this, "Creating your account...");
            functions.register(email.getText().toString().trim(), password.getText().toString(), data, task -> {
                functions.hideLoading(loading);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getActivity(), MainAppJobSeeker.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Could not create account. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }, auth -> {
                if (!auth.isSuccessful()) {
                    functions.hideLoading(loading);
                    String message = auth.getException() != null ? auth.getException().getMessage() : "Signup failed";
                    alert.setText(message);
                    alert.setVisibility(View.VISIBLE);
                }
            });
        });

        info.setOnClickListener(view ->
                needed.setVisibility(needed.getVisibility() == View.GONE ? View.VISIBLE : View.GONE)
        );

        return pageui;
    }
}
// ------------------------------------------
    package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;

import java.util.ArrayList;
import java.util.List;

    public class BusinessRegisterPage extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//      get the current page ui to show in the frame layout
            View pageui = inflater.inflate(R.layout._signup_bussiness_register__third_b_, container, false);
            Button full, half, temp, confirm, daily, hourly;
            EditText name, size, address, email, phone, pay;
            functions PressedCheck = new functions();

            full = pageui.findViewById(R.id.fulltime);
            half = pageui.findViewById(R.id.halftime);
            temp = pageui.findViewById(R.id.temporary);
            confirm = pageui.findViewById(R.id.confirm);
            daily = pageui.findViewById(R.id.daily);
            hourly = pageui.findViewById(R.id.hourly);
            name = pageui.findViewById(R.id.businessName);
            size = pageui.findViewById(R.id.businessSize);
            address = pageui.findViewById(R.id.businessAddress);
            email = pageui.findViewById(R.id.businessEmail);
            phone = pageui.findViewById(R.id.businessPhone);
            pay = pageui.findViewById(R.id.payrate);

            //        needs
            PressedCheck.pressed(full);
            PressedCheck.pressed(half);
            PressedCheck.pressed(temp);

            //        pay
            PressedCheck.pressed(daily);
            PressedCheck.pressed(hourly);

            //go to next fragment
            confirm.setOnClickListener(view -> {
                //needs list
                List<String> selectedneeds = new ArrayList<>();
                if (full.isSelected()) selectedneeds.add("Full-Time");
                if (half.isSelected()) selectedneeds.add("Half-Time");
                if (temp.isSelected()) selectedneeds.add("Temporary");

                //pay list
                List<String> selectedpay = new ArrayList<>();
                if (daily.isSelected()) selectedpay.add("Daily Pay");
                if (hourly.isSelected()) selectedpay.add("Hourly Pay");

                String nameText = name.getText().toString();
                String sizeText = size.getText().toString();
                String addressText = address.getText().toString();
                String emailText = email.getText().toString();
                String phoneText = phone.getText().toString();
                String payText = pay.getText().toString();

                // sending to next fragment
                Bundle bundle = new Bundle();
                bundle.putString("AccountType", "Business");
                bundle.putString("Name", nameText);
                bundle.putString("Size", sizeText);
                bundle.putString("Address", addressText);
                bundle.putString("Email", emailText);
                bundle.putString("Phone", phoneText);
                bundle.putString("Pay", payText);
                bundle.putStringArrayList("HiringNeeds", new ArrayList<>(selectedneeds));
                bundle.putStringArrayList("PayRate", new ArrayList<>(selectedpay));
                if (functions.Businessfilled(nameText, emailText, addressText,
                        sizeText, payText, new ArrayList<>(selectedneeds), new ArrayList<>(selectedpay))) {
                    functions.move(this, new BusinessRegisterPassword(), bundle);
                }
                else {
                    Toast.makeText(getActivity(), "Fill All The Information!", Toast.LENGTH_SHORT).show();
                }
            });

            return pageui;
        }
    }
// -------------------------------------------------
    package com.example.myapplication.loginandsignup;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.example.myapplication.mainapp_business.MainAppBusiness;

import java.util.HashMap;
import java.util.Map;

    public class BusinessRegisterPassword extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View pageui = inflater.inflate(R.layout._signup_bussines_register__third_b_password_, container, false);

            TextView showPassword = pageui.findViewById(R.id.ShowPassword);
            TextView alert = pageui.findViewById(R.id.alert);
            EditText password = pageui.findViewById(R.id.Password);
            EditText confirm = pageui.findViewById(R.id.ConfirmPassword);
            Button finish = pageui.findViewById(R.id.Finish);

            showPassword.setOnClickListener(view -> {
                boolean isHidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
                if (isHidden) {
                    functions.show(password, confirm);
                    showPassword.setText("Hide Password");
                } else {
                    functions.hide(password, confirm);
                    showPassword.setText("Show Password");
                }
            });

            finish.setOnClickListener(view -> {
                if (!functions.passwordmatch(password, confirm)) {
                    functions.alert(alert);
                    return;
                }

                Bundle bundle = getArguments();
                if (bundle == null) {
                    alert.setText("Missing business information. Please go back and try again.");
                    alert.setVisibility(View.VISIBLE);
                    return;
                }

                String accountType = functions.firstNonEmpty(bundle.getString("AccountType"), "Business");
                String email = functions.firstNonEmpty(bundle.getString("Email"), "").trim();
                Map<String, Object> data = new HashMap<>();
                data.put("Account Type", accountType);
                data.put("AccountType", accountType);
                data.put("Business Name", bundle.getString("Name"));
                data.put("Business Size", bundle.getString("Size"));
                data.put("Business Address", bundle.getString("Address"));
                data.put("Business Email", email);
                data.put("Business Phone", bundle.getString("Phone"));
                data.put("Business Pay", bundle.getString("Pay"));
                data.put("Hiring Needs", bundle.getStringArrayList("HiringNeeds"));
                data.put("Pay Rate", bundle.getStringArrayList("PayRate"));

                AlertDialog loading = functions.showLoading(this, "Creating business account...");
                functions.register(email, password.getText().toString(), data, task -> {
                    functions.hideLoading(loading);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(requireContext(), MainAppBusiness.class));
                        requireActivity().finish();
                    }
                }, auth -> {
                    if (!auth.isSuccessful()) {
                        functions.hideLoading(loading);
                        String message = auth.getException() != null ? auth.getException().getMessage() : "Signup failed";
                        alert.setText(message);
                        alert.setVisibility(View.VISIBLE);
                    }
                });
            });

            return pageui;
        }
    }
// ------------------------------------------------
    package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.google.firebase.auth.FirebaseAuth;

    public class ForgotPassword extends Fragment {
        @Nullable
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View pageui = inflater.inflate(R.layout._signup_forgot_password, container, false);

            EditText emailInput = pageui.findViewById(R.id.email);
            TextView alertText = pageui.findViewById(R.id.alert);
            Button resetButton = pageui.findViewById(R.id.resetBtn);
            TextView backToLogin = pageui.findViewById(R.id.backToLogin);

            resetButton.setOnClickListener(v -> {
                String email = emailInput.getText().toString().trim();

                if (email.isEmpty()) {
                    alertText.setVisibility(View.VISIBLE);
                    alertText.setText("Please enter your email.");
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    alertText.setVisibility(View.VISIBLE);
                    alertText.setText("Please enter a valid email address.");
                    return;
                }

                alertText.setVisibility(View.GONE);
                resetButton.setEnabled(false);
                resetButton.setText("Sending...");
                AlertDialog loading = functions.showLoading(this, "Sending reset link...");

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> {
                            functions.hideLoading(loading);
                            resetButton.setEnabled(true);
                            resetButton.setText("Send Reset Link");
                            Toast.makeText(getContext(), "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                            functions.move(this, new LoginPage(), null);
                        })
                        .addOnFailureListener(e -> {
                            functions.hideLoading(loading);
                            resetButton.setEnabled(true);
                            resetButton.setText("Send Reset Link");
                            alertText.setVisibility(View.VISIBLE);
                            alertText.setText(e.getMessage() == null ? "Failed to send reset email." : e.getMessage());
                        });
            });

            backToLogin.setOnClickListener(v -> functions.move(this, new LoginPage(), null));

            return pageui;
        }
    }
// ------------------------------------------------------------------------------
    package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

    public class HireOrSeek extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//      get the current page ui to show in the frame layout
            View pageui = inflater.inflate(R.layout._signup_recruit_or_seek__second_a_, container, false);

//      define buttons and text(to make it disappear
            Button recruit, seek;

//      assign them to their id's
            recruit = pageui.findViewById(R.id.Employee);
            seek = pageui.findViewById(R.id.Employer);

//      on click change the fragment to the desired layout
            recruit.setOnClickListener(view ->
            {
                getParentFragmentManager().beginTransaction().replace(R.id.baseframe, new com.example.myapplication.loginandsignup.BasicPersonalRegisterPage()).addToBackStack(null).commit();
            });

            seek.setOnClickListener(view ->
            {
                getParentFragmentManager().beginTransaction().replace(R.id.baseframe, new com.example.myapplication.loginandsignup.BusinessRegisterPage()).addToBackStack(null).commit();
            });

//      return the page ui
            return pageui;
        }
    }
// --------------------------------------------------------------------------
    package com.example.myapplication.loginandsignup;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

    public class LoginPage extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View pageui = inflater.inflate(R.layout._signup_login_page__second_b_, container, false);
            Button signin = pageui.findViewById(R.id.signin);
            EditText email = pageui.findViewById(R.id.Email);
            EditText password = pageui.findViewById(R.id.Password);
            TextView showpass = pageui.findViewById(R.id.ShowPassword);
            TextView reset = pageui.findViewById(R.id.resetPassword);
            TextView alert = pageui.findViewById(R.id.alert);

            showpass.setOnClickListener(view -> {
                boolean isHidden = password.getTransformationMethod() instanceof PasswordTransformationMethod;
                if (isHidden) {
                    password.setTransformationMethod(null);
                    showpass.setText("Hide Password");
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    showpass.setText("Show Password");
                }
            });

            reset.setOnClickListener(view -> functions.move(this, new com.example.myapplication.loginandsignup.ForgotPassword(), null));

            signin.setOnClickListener(view -> {
                String emailValue = email.getText().toString().trim();
                String passwordValue = password.getText().toString().trim();

                if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                    alert.setVisibility(View.VISIBLE);
                    alert.setText("Fill All The Fields!");
                    return;
                }

                AlertDialog loading = functions.showLoading(this, "Signing in...");
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailValue, passwordValue)
                        .addOnCompleteListener(task -> {
                            functions.hideLoading(loading);

                            if (!task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Incorrect Password or Email", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) {
                                Toast.makeText(getActivity(), "Login session failed. Please try again.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            functions.moveByType(this, user.getUid());
                        });
            });

            return pageui;
        }
    }
// ----------------------------------------------
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
                getParentFragmentManager().beginTransaction().replace(R.id.baseframe, new com.example.myapplication.loginandsignup.HireOrSeek()).addToBackStack(null).commit();
            });

            Signin.setOnClickListener(view -> {
                getParentFragmentManager().beginTransaction().replace(R.id.baseframe, new com.example.myapplication.loginandsignup.LoginPage()).addToBackStack(null).commit();
            });
            return pageui;
        }
    }

// -------------------------------------------------------------------
public static boolean passwordmatch(EditText password, EditText confirm) {


    return password.getText().toString().equals(confirm.getText().toString());
}

    //    show/hide password
    public static void show(EditText pass, EditText confirm) {
        pass.setTransformationMethod(null);
        confirm.setTransformationMethod(null);
    }

    public static void hide(EditText pass, EditText confirm) {
        pass.setTransformationMethod(new PasswordTransformationMethod());
        confirm.setTransformationMethod(new PasswordTransformationMethod());
    }

    //    fields filled personal
    public static boolean Personalfilled(EditText name, EditText email) {

        return !name.getText().toString().isEmpty() && !email.getText().toString().isEmpty();
    }

    //    fields filled business
    public static boolean Businessfilled(String name, String email, String address, String size, String pay,
                                         ArrayList<String> needs, ArrayList<String> rate) {

        return !name.isEmpty() && !email.isEmpty() && !address.isEmpty() &&
                !size.isEmpty() && !pay.isEmpty() && needs != null && rate != null;
    }

    //move to new fragment
    public static void move(Fragment currentFrag, Fragment targetFrag, @org.jetbrains.annotations.Nullable Bundle bundle) {
        // set arguments only if bundle exists
        if (bundle != null) {
            targetFrag.setArguments(bundle);
        }

        // replace fragment
        currentFrag.requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.baseframe, targetFrag)
                .addToBackStack(null)
                .commit();
    }
    //move in the app
    public static void moveApp(AppCompatActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }
    public static void moveMain(Fragment frag, Fragment fragment) {
        frag.requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }
    public static void moveMain(Fragment frag, Fragment fragment, @org.jetbrains.annotations.Nullable Bundle bundle) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        frag.requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }
    //  password not matching alert
    public static void alert(TextView alert) {
        alert.setVisibility(View.GONE);
        alert.setText("Passwords Do Not Match!");
        new Handler().postDelayed(() -> alert.setVisibility(View.VISIBLE), 100);
        return;
    }
    public static void register(String email, String password, Map<String, Object> data, OnCompleteListener<Void> listener, OnCompleteListener<AuthResult> authlistener) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            authlistener.onComplete(task);
            if (task.isSuccessful()) {
                FirebaseUser currentUser = task.getResult() != null ? task.getResult().getUser() : FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    listener.onComplete(Tasks.forException(new IllegalStateException("User session not available after signup.")));
                    return;
                }
                db.collection("users").document(currentUser.getUid()).set(data).addOnCompleteListener(listener);
            }
        });
    }
}
