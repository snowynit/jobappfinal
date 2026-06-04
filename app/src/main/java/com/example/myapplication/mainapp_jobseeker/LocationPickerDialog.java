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

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class LocationPickerDialog extends DialogFragment {

    public interface LocationSelectionListener {
        void onLocationSelected(double latitude, double longitude, double radiusKm);
        void onLocationCleared();
    }

    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_RADIUS = "radius";
    private static final GeoPoint DEFAULT_CENTER = new GeoPoint(32.0853, 34.7818);

    private MapView mapView;
    private Marker selectedMarker;
    private GeoPoint selectedPoint;
    private TextView selectedLocationText;
    private EditText radiusInput;
    private LocationListener activeLocationListener;
    private final Handler handler = new Handler(Looper.getMainLooper());
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

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    useCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission was denied.", Toast.LENGTH_SHORT).show();
                }
            });

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
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        return inflater.inflate(R.layout.dialog_location_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//      assign things
        selectedLocationText = view.findViewById(R.id.selectedLocationText);
        radiusInput = view.findViewById(R.id.radiusInput);
        Button applyButton = view.findViewById(R.id.applyLocationButton);
        Button clearButton = view.findViewById(R.id.clearLocationButton);
        Button currentLocationButton = view.findViewById(R.id.useCurrentLocationButton);
        mapView = view.findViewById(R.id.mapView);

        double radiusKm = getArguments() == null ? 10d : getArguments().getDouble(ARG_RADIUS, 10d);
        radiusInput.setText(String.format(Locale.US, "%.0f", radiusKm));

        setupMap();
        applyButton.setOnClickListener(v -> applySelection());
        currentLocationButton.setOnClickListener(v -> requestCurrentLocation());
        clearButton.setOnClickListener(v -> {
            if (getParentFragment() instanceof LocationSelectionListener) {
                ((LocationSelectionListener) getParentFragment()).onLocationCleared();
            }
            dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        stopLocationUpdates();
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    private void setupMap() {
        selectedPoint = readInitialPoint();

//      open street map settings
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(11.0);
        mapView.getController().setCenter(selectedPoint);

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint point) {
                selectedPoint = point;
                renderMarker(point);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint point) {
                selectedPoint = point;
                renderMarker(point);
                return true;
            }
        };
        mapView.getOverlays().add(new MapEventsOverlay(receiver));
        renderMarker(selectedPoint);
    }

    private GeoPoint readInitialPoint() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_LATITUDE) && args.containsKey(ARG_LONGITUDE)) {
            return new GeoPoint(args.getDouble(ARG_LATITUDE), args.getDouble(ARG_LONGITUDE));
        }
        return DEFAULT_CENTER;
    }

    private void renderMarker(GeoPoint point) {
        if (mapView == null || point == null) {
            return;
        }

        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }

        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(point);
        selectedMarker.setTitle("Search center");
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();
        selectedLocationText.setText(String.format(Locale.US, "Selected: %.5f, %.5f", point.getLatitude(), point.getLongitude()));
    }

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

    private void useCurrentLocation() {
        if (getContext() == null) {
            return;
        }
        if (!functions.hasLocationPermission(requireContext())) {
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

    private void moveToDeviceLocation(Location location) {
        if (location == null) {
            return;
        }

        selectedPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (mapView != null) {
            mapView.getController().animateTo(selectedPoint);
            mapView.getController().setZoom(12.0);
        }
        renderMarker(selectedPoint);
    }

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
                .onLocationSelected(selectedPoint.getLatitude(), selectedPoint.getLongitude(), radiusKm);
        dismiss();
    }
}
