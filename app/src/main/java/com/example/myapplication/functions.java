package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.mainapp_business.MainAppBusiness;
import com.example.myapplication.mainapp_jobseeker.Adapter.Job;
import com.example.myapplication.loginandsignup.SigninOrSignupChoiceButtons;
import com.example.myapplication.mainapp_jobseeker.MainAppJobSeeker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class functions {
    // חיבור לפיירסטור
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // בודק אם שתי הסיסמאות זהות
    public static boolean passwordmatch(EditText password, EditText confirm) {


        return password.getText().toString().equals(confirm.getText().toString());
    }

    // מציג / מסתיר את הסיסמה
    public static void show(EditText pass, EditText confirm) {
        pass.setTransformationMethod(null);
        confirm.setTransformationMethod(null);
    }

    public static void hide(EditText pass, EditText confirm) {
        pass.setTransformationMethod(new PasswordTransformationMethod());
        confirm.setTransformationMethod(new PasswordTransformationMethod());
    }

    // בודק שהשדות בהרשמה אישית מלאים
    public static boolean Personalfilled(EditText name, EditText email) {

        return !name.getText().toString().isEmpty() && !email.getText().toString().isEmpty();
    }

    // בודק שהשדות בהרשמה של עסק מלאים
    public static boolean Businessfilled(String name, String email, String address, String size, String pay,
                                         ArrayList<String> needs, ArrayList<String> rate) {

        return !name.isEmpty() && !email.isEmpty() && !address.isEmpty() &&
                !size.isEmpty() && !pay.isEmpty() && needs != null && rate != null;
    }

    // מעבר ל-fragment חדש במסך ההתחברות
    public static void move(Fragment currentFrag, Fragment targetFrag, @Nullable Bundle bundle) {
        if (bundle != null) {
            targetFrag.setArguments(bundle);
        }
        currentFrag.requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.baseframe, targetFrag)
                .addToBackStack(null)
                .commit();
    }
    // מעבר בתוך האפליקציה הראשית (מתוך Activity)
    public static void moveApp(AppCompatActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }
    // מעבר בין fragments בתוך האפליקציה הראשית
    public static void moveMain(Fragment frag, Fragment fragment) {
        frag.requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }
    // אותו דבר אבל מעביר גם נתונים ב-Bundle
    public static void moveMain(Fragment frag, Fragment fragment, @Nullable Bundle bundle) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        frag.requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.mainframe, fragment)
                .addToBackStack(null)
                .commit();
    }

    // פותח את האפליקציה הנכונה לפי סוג המשתמש (עסק / מחפש עבודה)
    public static void moveByType(AppCompatActivity activity, String id) {
        FirebaseFirestore.getInstance().collection("users").document(id).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String type = getValue(
                        document.getString("Account Type"),
                        document.getString("AccountType"),
                        document.getString("accountType")
                );
                if ("Business".equals(type)) {
                    activity.startActivity(new Intent(activity, MainAppBusiness.class));
                    activity.finish();
                } else {
                    activity.startActivity(new Intent(activity, MainAppJobSeeker.class));
                    activity.finish();
                }
            } else {
                int frameId = activity.findViewById(R.id.baseframe) != null ? R.id.baseframe : R.id.mainframe;
                activity.getSupportFragmentManager().beginTransaction().replace(frameId, new SigninOrSignupChoiceButtons()).commit();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();

            int frameId = activity.findViewById(R.id.baseframe) != null ? R.id.baseframe : R.id.mainframe;
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(frameId, new SigninOrSignupChoiceButtons())
                    .commit();
        });
    }

    // אותו דבר אבל קוראים מ-Fragment
    public static void moveByType(Fragment fragment, String id) {
        moveByType((AppCompatActivity) fragment.requireActivity(), id);
    }

    // הודעת שגיאה כשהסיסמאות לא תואמות
    public static void alert(TextView alert) {
        alert.setVisibility(View.GONE);
        alert.setText("Passwords Do Not Match!");
        new Handler().postDelayed(() -> alert.setVisibility(View.VISIBLE), 100);
        return;
    }

    // יוצר משתמש חדש בפיירבייס ושומר את הפרטים שלו בפיירסטור
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

    // מחבר ללחיצה את שינוי הצבע של הכפתור
    // sends reset password email
    public static void sendPasswordReset(Fragment fragment, String email, TextView alert, TextView button, String normalText) {
        if (email.isEmpty()) {
            alert.setVisibility(View.VISIBLE);
            alert.setText("Please enter your email first.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            alert.setVisibility(View.VISIBLE);
            alert.setText("Please enter a valid email.");
            return;
        }

        alert.setVisibility(View.GONE);
        button.setEnabled(false);
        button.setText("Sending...");
        AlertDialog loading = showLoading(fragment, "Sending reset link...");

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    hideLoading(loading);
                    button.setEnabled(true);
                    button.setText(normalText);
                    Toast.makeText(fragment.getContext(), "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    hideLoading(loading);
                    button.setEnabled(true);
                    button.setText(normalText);
                    alert.setVisibility(View.VISIBLE);
                    alert.setText(e.getMessage() == null ? "Failed to send reset email." : e.getMessage());
                });
    }

    public static void pressed(Button button) {
        if (button == null) {
            return;
        }
        styleButton(button, button.isSelected());
        button.setOnClickListener(view -> styleButton(button, !button.isSelected()));
    }

    public static void styleButton(Button button) {
        pressed(button);
    }

    // צובע את הכפתור לפי האם הוא נבחר או לא
    public static void styleButton(Button button, boolean isSelected) {
        if (button == null) {
            return;
        }
        button.setSelected(isSelected);
        button.setBackgroundTintList(null);
        button.setAlpha(1f);

        int blue = Color.parseColor("#2563EB");
        float density = button.getResources().getDisplayMetrics().density;

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(14 * density);

        if (isSelected) {
            shape.setColor(blue);
            button.setTextColor(Color.WHITE);
        } else {
            shape.setColor(Color.WHITE);
            shape.setStroke((int) density, Color.parseColor("#E5E7EB"));
            button.setTextColor(Color.parseColor("#111827"));
        }

        button.setBackground(shape);
    }

    // מחזיר את הטקסט באותיות קטנות (גם אם זה null)
    public static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    // מחזיר את הערך הראשון שלא ריק (פיירסטור לפעמים שולח שמות שדות שונים)
    public static String getValue(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    // לוקח את הערך הראשון מתוך רשימה של פיירסטור
    public static String firstFromList(Object value) {
        if (value instanceof List && !((List<?>) value).isEmpty()) {
            Object first = ((List<?>) value).get(0);
            return first == null ? "" : first.toString().trim();
        }
        return "";
    }

    // בונה אובייקט Job מתוך מסמך של פיירסטור
    public static Job mapJobDocument(DocumentSnapshot document) {
        Job job = new Job();
        job.id = document.getId();
        job.ownerId = getValue(
                document.getString("ownerId"),
                document.getString("businessId"),
                document.getString("postedBy"),
                document.getString("userId")
        );
        job.title = getValue(
                document.getString("title"),
                document.getString("jobTitle"),
                document.getString("Job Title"),
                document.getString("role"),
                document.getString("Role")
        );
        job.description = getValue(
                document.getString("description"),
                document.getString("jobDescription"),
                document.getString("Job Description"),
                document.getString("about"),
                document.getString("About")
        );
        job.businessName = getValue(
                document.getString("businessName"),
                document.getString("Business Name"),
                document.getString("company"),
                document.getString("Company")
        );
        job.type = getValue(
                document.getString("type"),
                document.getString("jobType"),
                document.getString("Job Type"),
                firstFromList(document.get("Hiring Needs"))
        );
        job.pay = getValue(
                document.getString("pay"),
                document.getString("salary"),
                document.getString("Business Pay"),
                document.getString("Pay")
        );
        job.address = getValue(
                document.getString("address"),
                document.getString("Address"),
                document.getString("Business Address")
        );
        job.location = getValue(
                document.getString("location"),
                document.getString("Location"),
                job.address
        );
        job.latitude = firstDouble(
                document.get("latitude"),
                document.get("lat"),
                document.get("Latitude"),
                document.get("Lat")
        );
        job.longitude = firstDouble(
                document.get("longitude"),
                document.get("lng"),
                document.get("Longitude"),
                document.get("Lng")
        );
        job.phone = getValue(
                document.getString("phone"),
                document.getString("Phone"),
                document.getString("PhoneNumber"),
                document.getString("Business Phone")
        );
        job.email = getValue(
                document.getString("email"),
                document.getString("Email"),
                document.getString("Business Email")
        );
        job.jobState = getValue(
                document.getString("jobState"),
                document.getString("Job State"),
                document.getBoolean("jobOpen") != null && !document.getBoolean("jobOpen") ? "Paused" : "Active"
        );

        if (job.businessName.isEmpty() && job.title.isEmpty() && job.description.isEmpty()) {
            return null;
        }
        if (job.title.isEmpty()) {
            job.title = "Open Position";
        }
        if (job.description.isEmpty()) {
            job.description = "Tap into this opportunity to learn more about the role.";
        }
        return job;
    }

    // אורז Job בתוך Bundle כדי להעביר בין fragments
    public static Bundle jobToBundle(Job job) {
        Bundle bundle = new Bundle();
        bundle.putString("job_id", job.id);
        bundle.putString("job_title", job.title);
        bundle.putString("job_description", job.description);
        bundle.putString("job_type", job.type);
        bundle.putString("job_pay", job.pay);
        bundle.putString("job_business_name", job.businessName);
        bundle.putString("job_owner_id", job.ownerId);
        bundle.putString("job_location", job.location);
        bundle.putDouble("job_latitude", job.latitude);
        bundle.putDouble("job_longitude", job.longitude);
        bundle.putString("job_address", job.address);
        bundle.putString("job_phone", job.phone);
        bundle.putString("job_email", job.email);
        bundle.putString("job_status", job.status);
        bundle.putString("job_state", job.jobState);
        bundle.putString("job_action_type", job.actionType);
        bundle.putString("job_saved_date", job.savedDate);
        bundle.putString("job_applicant_name", job.applicantName);
        bundle.putString("job_applicant_id", job.applicantId);
        bundle.putString("job_resume_name", job.resumeName);
        bundle.putString("job_resume_text", job.resumeText);
        bundle.putString("job_resume_source", job.resumeSource);
        return bundle;
    }

    // מוציא בחזרה Job מתוך Bundle
    public static Job jobFromBundle(Bundle bundle) {
        Job job = new Job();
        job.id = bundle.getString("job_id", "");
        job.title = bundle.getString("job_title", "");
        job.description = bundle.getString("job_description", "");
        job.type = bundle.getString("job_type", "");
        job.pay = bundle.getString("job_pay", "");
        job.businessName = bundle.getString("job_business_name", "");
        job.ownerId = bundle.getString("job_owner_id", "");
        job.location = bundle.getString("job_location", "");
        job.latitude = bundle.getDouble("job_latitude", 0d);
        job.longitude = bundle.getDouble("job_longitude", 0d);
        job.address = bundle.getString("job_address", "");
        job.phone = bundle.getString("job_phone", "");
        job.email = bundle.getString("job_email", "");
        job.status = bundle.getString("job_status", "");
        job.jobState = bundle.getString("job_state", "");
        job.actionType = bundle.getString("job_action_type", "");
        job.savedDate = bundle.getString("job_saved_date", "");
        job.applicantName = bundle.getString("job_applicant_name", "");
        job.applicantId = bundle.getString("job_applicant_id", "");
        job.resumeName = bundle.getString("job_resume_name", "");
        job.resumeText = bundle.getString("job_resume_text", "");
        job.resumeSource = bundle.getString("job_resume_source", "");
        return job;
    }

    // מקבל את שם הקובץ מ-URI (שימושי בהעלאת קורות חיים)
    public static String getDisplayName(Context context, Uri uri) {
        if (uri == null) {
            return "";
        }

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst() && nameIndex != -1) {
                String name = cursor.getString(nameIndex);
                cursor.close();
                return name == null ? "" : name;
            }
            cursor.close();
        }
        return getValue(uri.getLastPathSegment(), "Selected file");
    }

    // מציג תמונה מ-URI, אם אין מציג תמונת ברירת מחדל
    public static void loadImageUri(ImageView imageView, String uriString, int fallbackRes) {
        if (uriString == null || uriString.trim().isEmpty()) {
            imageView.setImageResource(fallbackRes);
            return;
        }

        try {
            imageView.setImageURI(Uri.parse(uriString));
        } catch (Exception e) {
            imageView.setImageResource(fallbackRes);
        }
    }

    // שומר הרשאת קריאה ל-URI גם אחרי שהאפליקציה נסגרת
    public static void persistReadPermission(Context context, Uri uri) {
        if (context == null || uri == null) {
            return;
        }
        try {
            context.getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
            // Some providers do not grant persistable permissions; loading can still work for current session.
        }
    }

    // מעתיק תמונה שנבחרה לתיקייה הפנימית של האפליקציה
    public static String saveImageToAppStorage(Context context, Uri sourceUri) {
        if (context == null || sourceUri == null) {
            return "";
        }

        try {
            File directory = new File(context.getFilesDir(), "profile_images");
            if (!directory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                directory.mkdirs();
            }

            File imageFile = new File(directory, "img_" + System.currentTimeMillis() + ".jpg");
            try (InputStream input = context.getContentResolver().openInputStream(sourceUri);
                 OutputStream output = new FileOutputStream(imageFile)) {
                if (input == null) {
                    return "";
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }

            return Uri.fromFile(imageFile).toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    // שומר את התמונה שנבחרה ומציג אותה במקום הנכון
    public static String pickImage(Context context, Uri uri, ImageView imageView, int fallbackRes) {
        String localUri = saveImageToAppStorage(context, uri);
        if (!localUri.isEmpty()) {
            loadImageUri(imageView, localUri, fallbackRes);
        }
        return localUri;
    }

    // הופך טקסט עם פסיקים לרשימה נקייה
    public static List<String> splitFields(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts = value.toLowerCase(Locale.ROOT).split(",");
        List<String> fields = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                fields.add(trimmed);
            }
        }
        return fields;
    }

    // ציון פשוט שמסתכל כמה משרה מתאימה להעדפות של המשתמש
    public static int recommendationScore(Job job, String preferredType, String fields, String desiredPay) {
        int score = 0;
        String jobType = safeLower(job.type).replace("-", "_").replace(" ", "_");
        String wantedType = safeLower(preferredType).replace("-", "_").replace(" ", "_");

        if (!wantedType.isEmpty() && jobType.contains(wantedType)) {
            score += 5;
        }

        String haystack = safeLower(getValue(job.title, "")) + " "
                + safeLower(getValue(job.description, "")) + " "
                + safeLower(getValue(job.businessName, ""));

        for (String field : splitFields(fields)) {
            if (haystack.contains(field)) {
                score += 3;
            }
        }

        int desired = extractNumber(desiredPay);
        int offered = extractNumber(job.pay);
        if (desired > 0 && offered >= desired) {
            score += 2;
        }

        if (!job.businessName.isEmpty()) {
            score += 1;
        }

        return score;
    }

    // לוקח רק את המספרים מתוך טקסט (לדוגמה "₪80/hr" → 80)
    public static int extractNumber(String value) {
        if (value == null) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ממיין משרות לפי הציון הכי גבוה קודם
    public static void sortRecommendedJobs(List<Job> jobs, String preferredType, String fields, String desiredPay) {
        Collections.sort(jobs, (first, second) -> Integer.compare(
                recommendationScore(second, preferredType, fields, desiredPay),
                recommendationScore(first, preferredType, fields, desiredPay)
        ));
    }

    // בודק אם המשתמש בנה קו"ח בתוך האפליקציה
    public static boolean hasBuiltResume(DocumentSnapshot document) {
        if (document == null) {
            return false;
        }
        return !getValue(document.getString("ResumeText"), "").isEmpty();
    }

    // בודק אם המשתמש העלה קובץ קו"ח
    public static boolean hasUploadedResume(DocumentSnapshot document) {
        if (document == null) {
            return false;
        }
        return !getValue(document.getString("ResumeUri"), "").isEmpty();
    }

    // בוחר כיתוב להצגה לקו"ח של המשתמש
    public static String getResumeLabel(DocumentSnapshot document) {
        if (document == null) {
            return "No resume selected";
        }
        String resumeName = getValue(document.getString("ResumeName"), "");
        if (!resumeName.isEmpty()) {
            return resumeName;
        }
        if (hasBuiltResume(document)) {
            return "In-app resume ready";
        }
        return "No resume selected";
    }

    // מחבר את כל החלקים של הקו"ח לטקסט אחד
    public static String buildResumeText(String fullName,String headline,String phone,String email,String location,String summary,String skills,String experience,String education,String languages) {
        StringBuilder builder = new StringBuilder();

        appendResumeSection(builder, getValue(fullName, "Your Name"));
        appendResumeSection(builder, headline);

        List<String> contact = new ArrayList<>();
        if (!getValue(phone, "").isEmpty()) {
            contact.add(phone.trim());
        }
        if (!getValue(email, "").isEmpty()) {
            contact.add(email.trim());
        }
        if (!getValue(location, "").isEmpty()) {
            contact.add(location.trim());
        }
        if (!contact.isEmpty()) {
            appendResumeSection(builder, joinWithSeparator(contact, " | "));
        }

        appendLabeledSection(builder, "Professional Summary", summary);
        appendLabeledSection(builder, "Skills", normalizeResumeList(skills));
        appendLabeledSection(builder, "Work Experience", experience);
        appendLabeledSection(builder, "Education", education);
        appendLabeledSection(builder, "Languages", normalizeResumeList(languages));

        return builder.toString().trim();
    }

    // עוזר: מוסיף קטע עם כותרת לקו"ח
    private static void appendLabeledSection(StringBuilder builder, String title, String body) {
        String safeBody = getValue(body, "");
        if (safeBody.isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("\n\n");
        }
        builder.append(title).append("\n").append(safeBody.trim());
    }

    // עוזר: מוסיף שורה רגילה (כמו שם או טלפון) לקו"ח
    private static void appendResumeSection(StringBuilder builder, String value) {
        String safeValue = getValue(value, "");
        if (safeValue.isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("\n");
        }
        builder.append(safeValue.trim());
    }

    // הופך "java, kotlin, swift" ל-"java • kotlin • swift"
    public static String normalizeResumeList(String value) {
        String safeValue = getValue(value, "");
        if (safeValue.isEmpty()) {
            return "";
        }

        String[] parts = safeValue.split(",");
        List<String> cleaned = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        return joinWithSeparator(cleaned, " \u2022 ");
    }

    // מחבר טקסטים עם מפריד ביניהם
    public static String joinWithSeparator(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }

    // מחזיר את הערך הראשון שאפשר להמיר למספר
    public static double firstDouble(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            try {
                return Double.parseDouble(value.toString().trim());
            } catch (Exception ignored) {
            }
        }
        return 0d;
    }

    // בודק אם המשתמש אישר הרשאת מיקום
    public static boolean hasLocationPermission(Context context) {
        return context != null &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // מביא את המיקום האחרון הידוע מ-GPS או מהרשת
    public static Location getBestLastLocation(Context context) {
        if (context == null || !hasLocationPermission(context)) {
            return null;
        }

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return null;
        }

        Location gps = null;
        Location network = null;
        try {
            gps = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }
        try {
            network = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        if (gps == null) {
            return network;
        }
        if (network == null) {
            return gps;
        }
        if (gps.getTime() >= network.getTime()) {
            return gps;
        }
        return network;
    }

    // מבקש עדכון מיקום חד-פעמי מ-GPS + רשת
    public static boolean askForOneLocation(Context context, LocationListener listener) {
        if (context == null || listener == null || !hasLocationPermission(context)) {
            return false;
        }

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return false;
        }

        boolean gps = askProvider(manager, LocationManager.GPS_PROVIDER, listener);
        boolean network = askProvider(manager, LocationManager.NETWORK_PROVIDER, listener);
        return gps || network;
    }

    // עוזר: מבקש מיקום מ-provider אחד
    private static boolean askProvider(LocationManager manager, String provider, LocationListener listener) {
        try {
            if (!manager.isProviderEnabled(provider)) {
                return false;
            }
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    // מפסיק להאזין לעדכוני מיקום
    public static void stopLocation(Context context, LocationListener listener) {
        if (context == null || listener == null) {
            return;
        }
        try {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (manager != null) {
                manager.removeUpdates(listener);
            }
        } catch (Exception ignored) {
        }
    }

    // בודק אם המשרה בתוך הרדיוס שבחרנו מהמיקום
    public static boolean jobInRadius(Job job, double lat, double lng, double radiusKm) {
        if (job == null || radiusKm <= 0d) {
            return true;
        }
        if (job.latitude == 0d && job.longitude == 0d) {
            return false;
        }

        float[] result = new float[1];
        Location.distanceBetween(lat, lng, job.latitude, job.longitude, result);
        return result[0] <= radiusKm * 1000d;
    }

    // בונה Job ממסמך של מועמדות (ככה אפשר להציג אותם ברשימה)
    public static Job mapApplicationDocument(DocumentSnapshot document) {
        Job job = new Job();
        job.id = document.getId();
        job.ownerId = getValue(document.getString("businessId"), "");
        job.title = getValue(document.getString("title"), "Open Position");
        job.description = getValue(document.getString("description"), "");
        job.type = getValue(document.getString("type"), "");
        job.pay = getValue(document.getString("pay"), "");
        job.businessName = getValue(document.getString("businessName"), "");
        job.location = getValue(document.getString("location"), "");
        job.address = getValue(document.getString("address"), "");
        job.phone = getValue(document.getString("phone"), "");
        job.email = getValue(document.getString("email"), "");
        job.status = getValue(document.getString("status"), "Pending");
        job.applicantName = getValue(document.getString("applicantName"), "Applicant");
        job.applicantId = getValue(document.getString("applicantId"), document.getString("userId"));
        job.resumeName = getValue(document.getString("resumeName"), "Resume attached");
        job.resumeText = getValue(document.getString("resumeText"), "");
        job.resumeSource = getValue(document.getString("resumeSource"), "");
        Timestamp appliedAt = document.getTimestamp("appliedAt");
        job.savedDate = appliedAt == null ? "Recently" : new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(appliedAt.toDate());
        return job;
    }

    // מציג חלון טעינה עם גלגל וטקסט
    public static AlertDialog showLoading(Fragment fragment, String message) {
        if (fragment == null || fragment.getContext() == null) {
            return null;
        }

        Context context = fragment.requireContext();
        float density = context.getResources().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        int padding = Math.round(20 * density);
        container.setPadding(padding, padding, padding, padding);
        container.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        container.addView(progressBar);

        TextView textView = new TextView(context);
        textView.setText(getValue(message, "Loading..."));
        textView.setTextSize(15);
        textView.setPadding(Math.round(14 * density), 0, 0, 0);
        container.addView(textView);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(container)
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }

    // סוגר את חלון הטעינה
    public static void hideLoading(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // יוצא מהחשבון של פיירבייס
    public static void logout(Fragment frag) {
        FirebaseAuth.getInstance().signOut();
    }
}
