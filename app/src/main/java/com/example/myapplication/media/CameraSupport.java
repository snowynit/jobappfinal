package com.example.myapplication.media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;

/**
 * Fragment-bound helper for picking a photo from the camera or gallery.
 * Usage in a Fragment:
 *
 *   private final CameraSupport photo = new CameraSupport(this, uri -> { ... });
 *   uploadArea.setOnClickListener(v -> photo.show());
 */
public class CameraSupport {

    public interface OnPick {
        void onPhotoPicked(Uri uri);
    }

    private final Fragment fragment;
    private final OnPick callback;
    private Uri pendingCameraUri;

    private final ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<Uri> cameraLauncher;
    private final ActivityResultLauncher<String> permissionLauncher;

    // רושם את ה-launchers של המצלמה, הגלריה וההרשאות (חייב להיות בפתיחת ה-Fragment)
    public CameraSupport(Fragment fragment, OnPick callback) {
        this.fragment = fragment;
        this.callback = callback;

        galleryLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) callback.onPhotoPicked(uri); });

        cameraLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && pendingCameraUri != null) {
                        callback.onPhotoPicked(pendingCameraUri);
                    }
                    pendingCameraUri = null;
                });

        permissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> { if (granted) launchCamera(); });
    }

    // מציג חלון לבחירה בין מצלמה לגלריה
    public void show() {
        if (fragment.getContext() == null) return;
        new AlertDialog.Builder(fragment.requireContext())
                .setTitle("Add a photo")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (d, which) -> {
                    if (which == 0) {
                        if (hasCameraPermission()) launchCamera();
                        else permissionLauncher.launch(Manifest.permission.CAMERA);
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // בודק אם נתנו הרשאה למצלמה
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                fragment.requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // מכין קובץ זמני דרך FileProvider ופותח את המצלמה
    private void launchCamera() {
        if (fragment.getContext() == null) return;
        File dir = new File(fragment.requireContext().getCacheDir(), "camera");
        if (!dir.exists() && !dir.mkdirs()) return;
        File output = new File(dir, "capture_" + System.currentTimeMillis() + ".jpg");
        try {
            if (!output.exists() && !output.createNewFile()) return;
        } catch (IOException e) {
            return;
        }
        String authority = fragment.requireContext().getPackageName() + ".fileprovider";
        pendingCameraUri = FileProvider.getUriForFile(fragment.requireContext(), authority, output);
        cameraLauncher.launch(pendingCameraUri);
    }
}
