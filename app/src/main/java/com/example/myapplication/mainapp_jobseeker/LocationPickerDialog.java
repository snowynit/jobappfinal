package com.example.myapplication.mainapp_jobseeker;

import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.functions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class LocationPickerDialog extends DialogFragment implements OnMapReadyCallback {

    // ממשק חזרה — איך הדיאלוג מחזיר תוצאה ל-fragment שפתח אותו
    public interface LocationSelectionListener {
        void onLocationSelected(double latitude, double longitude, double radiusKm);
        void onLocationCleared();
    }

    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_RADIUS = "radius";
    // נקודת ברירת מחדל — תל אביב
    private static final LatLng DEFAULT_CENTER = new LatLng(32.0853, 34.7818);

    private MapView mapView;
    private GoogleMap googleMap;
    private Marker selectedMarker;
    private LatLng selectedPoint;
    private TextView selectedLocationText;
    private EditText radiusInput;
    private LocationListener activeLocationListener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    // אם לוקח יותר מ-8 שניות לקבל מיקום — מנסים fallback למיקום אחרון ידוע
    private final Runnable locationTimeoutRunnable = () -> {
        if (activeLocationListener != null) {
            stopLocationUpdates();
            Location fallback = functions.getBestLastLocation(getContext());
            if (fallback != null) {
                moveToDeviceLocation(fallback);
            } else {
                Toast.makeText(getContext(), "Could not get location yet. Try setting emulator location.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // launcher של בקשת הרשאת מיקום
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    useCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission was denied.", Toast.LENGTH_SHORT).show();
                }
            });

    // יצירת הדיאלוג עם פרמטרים (מיקום קודם אם יש, ורדיוס)
    public static LocationPickerDialog newInstance(@Nullable Double latitude, @Nullable Double longitude, double radiusKm) {
        LocationPickerDialog dialog = new LocationPickerDialog();
        Bundle args = new Bundle();
        if (latitude != null && longitude != null) {
            args.putDouble(ARG_LATITUDE, latitude);
            args.putDouble(ARG_LONGITUDE, longitude);
        }
        args.putDouble(ARG_RADIUS, radiusKm > 0d ? radiusKm : 10d);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_location_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // חיבור הרכיבים מה-XML
        selectedLocationText = view.findViewById(R.id.selectedLocationText);
        radiusInput = view.findViewById(R.id.radiusInput);
        Button applyButton = view.findViewById(R.id.applyLocationButton);
        Button clearButton = view.findViewById(R.id.clearLocationButton);
        Button currentLocationButton = view.findViewById(R.id.useCurrentLocationButton);
        mapView = view.findViewById(R.id.mapView);

        double radiusKm = getArguments() == null ? 10d : getArguments().getDouble(ARG_RADIUS, 10d);
        radiusInput.setText(String.format(Locale.US, "%.0f", radiusKm));

        // אתחול המפה של גוגל — חובה לקרוא onCreate ואז getMapAsync
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        applyButton.setOnClickListener(v -> applySelection());
        currentLocationButton.setOnClickListener(v -> requestCurrentLocation());
        clearButton.setOnClickListener(v -> {
            if (getParentFragment() instanceof LocationSelectionListener) {
                ((LocationSelectionListener) getParentFragment()).onLocationCleared();
            }
            dismiss();
        });
    }

    // נקרא כשהמפה מוכנה לשימוש (אסינכרוני)
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        selectedPoint = readInitialPoint();

        // מציב את המצלמה על הנקודה ההתחלתית
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPoint, 11f));

        // לחיצה על המפה בוחרת נקודה חדשה
        googleMap.setOnMapClickListener(latLng -> {
            selectedPoint = latLng;
            renderMarker(latLng);
        });

        renderMarker(selectedPoint);
    }

    // קוראים את הנקודה ההתחלתית מ-arguments, אם לא קיימת — תל אביב
    private LatLng readInitialPoint() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_LATITUDE) && args.containsKey(ARG_LONGITUDE)) {
            return new LatLng(args.getDouble(ARG_LATITUDE), args.getDouble(ARG_LONGITUDE));
        }
        return DEFAULT_CENTER;
    }

    // מסיר Marker קודם אם קיים ומציב חדש בנקודה
    private void renderMarker(LatLng point) {
        if (googleMap == null || point == null) {
            return;
        }
        if (selectedMarker != null) {
            selectedMarker.remove();
        }
        selectedMarker = googleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Search center"));
        selectedLocationText.setText(String.format(Locale.US, "Selected: %.5f, %.5f", point.latitude, point.longitude));
    }

    // בודק אם יש הרשאה ל-FINE_LOCATION; אם לא — מבקש
    private void requestCurrentLocation() {
        if (getContext() == null) {
            return;
        }
        if (functions.hasLocationPermission(requireContext())) {
            useCurrentLocation();
            return;
        }
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    // מבקש מיקום נוכחי (חד פעמי)
    private void useCurrentLocation() {
        if (getContext() == null || !functions.hasLocationPermission(requireContext())) {
            return;
        }
        stopLocationUpdates();

        activeLocationListener = location -> {
            stopLocationUpdates();
            moveToDeviceLocation(location);
        };

        boolean requested = functions.askForOneLocation(requireContext(), activeLocationListener);

        if (!requested) {
            Location fallback = functions.getBestLastLocation(requireContext());
            if (fallback != null) {
                moveToDeviceLocation(fallback);
            } else {
                Toast.makeText(getContext(), "Turn on device location or set emulator location.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Toast.makeText(getContext(), "Finding your location...", Toast.LENGTH_SHORT).show();
        handler.postDelayed(locationTimeoutRunnable, 8000);
    }

    private void stopLocationUpdates() {
        handler.removeCallbacks(locationTimeoutRunnable);
        if (activeLocationListener == null || getContext() == null) {
            activeLocationListener = null;
            return;
        }
        functions.stopLocation(requireContext(), activeLocationListener);
        activeLocationListener = null;
    }

    // מקבל את המיקום של המשתמש וזז אליו במפה
    private void moveToDeviceLocation(Location location) {
        if (location == null) {
            return;
        }
        selectedPoint = new LatLng(location.getLatitude(), location.getLongitude());
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPoint, 12f));
        }
        renderMarker(selectedPoint);
    }

    // שולח חזרה את הנקודה והרדיוס ל-fragment שפתח את הדיאלוג
    private void applySelection() {
        if (!(getParentFragment() instanceof LocationSelectionListener) || selectedPoint == null) {
            dismiss();
            return;
        }
        double radiusKm = functions.firstDouble(radiusInput.getText().toString());
        if (radiusKm <= 0d) {
            radiusKm = 10d;
        }
        ((LocationSelectionListener) getParentFragment())
                .onLocationSelected(selectedPoint.latitude, selectedPoint.longitude, radiusKm);
        dismiss();
    }

    // ====== מחזור החיים של MapView — חובה ב-Google Maps ======

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        stopLocationUpdates();
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapView != null) mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
