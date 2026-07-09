package com.example.maptrackapplication;

import com.example.maptrackapplication.map.MarkerManager;
import com.example.maptrackapplication.network.NetworkUtils;
import com.example.maptrackapplication.network.RouteService;
import com.example.maptrackapplication.network.SearchService;
import com.example.maptrackapplication.analytics.MovementAnalyzer;
import android.location.LocationManager;
import android.Manifest;
import java.util.ArrayList;
import android.os.Build;
import android.content.SharedPreferences;//small db inside app
import android.content.pm.PackageManager;

import android.os.Bundle;
import androidx.core.view.WindowCompat;//for edge to edge(under status bar)
import android.widget.*;
import androidx.core.view.GravityCompat;//for drawer open close

import android.app.AlertDialog;

import android.content.Intent;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.CustomZoomButtonsController;

import org.osmdroid.events.DelayedMapListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;//Runtime permissions
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;


public class MainActivity extends AppCompatActivity {
    private MarkerManager markerManager;
    private MapView map;
    private Marker liveMarker, searchMarker, trackedMarker;
    private GeoPoint currentLocation;
    private GeoPoint searchedLocation;
    private LinearLayout btnStopTrackingRemote,
            btnDrawRoute,
            btnClearMarkers;
    private LinearLayout btnTrack, btnShare, btnClearRoute, btnAllowTracking;
    private ImageButton btnMyLocation, btnZoomIn, btnZoomOut;
    private ImageView menuBtn, searchIcon;
    private EditText searchBox;
    private DrawerLayout drawerLayout;
    TextView txtAllowTracking;
    private boolean firstTrackedFix = true;
    private LocationHelper locationHelper;
    private Polyline routeLine;
    private boolean followUser = true;
    private LinearLayout btnEditName;
    private MapViewModel viewModel;
    private AppRepository repository;

    private String myUserId;
    private boolean firstFix = true;
    private String currentlyTrackingId;
    private ValueEventListener trackingListener;
    private MovementAnalyzer movementAnalyzer;
    private TextView txtMovementInsight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );
        getWindow().setNavigationBarColor(
                0x11000000);
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    100
            );
        }
        setupUser();
        FirebaseApp.initializeApp(this);

        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        repository = new AppRepository(myUserId);
        movementAnalyzer = new MovementAnalyzer();


        setupMap();
        setupViews();
        SharedPreferences prefs =
                getSharedPreferences("MyApp", MODE_PRIVATE);

        boolean savedTracking =
                prefs.getBoolean(
                        "tracking_enabled",
                        false
                );

        if (savedTracking) {

            viewModel.toggleTracking();

            repository.setTracking(true);

            txtAllowTracking.setText(
                    "Stop Sharing Live Location"
            );

            if (locationHelper != null) {

                locationHelper.stop();
            }

            locationHelper = new LocationHelper(this);

            locationHelper.start(point -> {

                viewModel.updateCurrentLocation(point);
            });
        }
        setupListeners();

        observeViewModel();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationHelper = new LocationHelper(this);
            locationHelper.start(point -> viewModel.updateCurrentLocation(point));

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    // ================= OBSERVER =================

    private void observeViewModel() {

        viewModel.getCurrentLocation().observe(this, point -> {

            if (point == null) return;

            currentLocation = point;

            if (liveMarker == null) {
                liveMarker =
                        markerManager.createLiveMarker(point);
                map.getController().setCenter(point);
                map.getController().setZoom(17.0);
            }

            liveMarker.setPosition(point);

            if (firstFix) {

                map.getController().setZoom(17.0);

                map.getController().animateTo(point);

                firstFix = false;
            }
            map.invalidate();
        });

        viewModel.getTrackedLocation().observe(this, point -> {

            if (point == null) return;

            if (trackedMarker == null) {
                trackedMarker =
                        markerManager.createTrackedMarker(point);
            }

            trackedMarker.setPosition(point);
            movementAnalyzer.addPoint(point);
            String insight =
                    movementAnalyzer.generateInsight();
            txtMovementInsight.setText(insight);
            if (firstTrackedFix) {

                map.getController().animateTo(point);

                map.getController().setZoom(17.0);

                firstTrackedFix = false;
            }
            map.invalidate();
        });
    }

    // ================= SETUP =================

    private void setupUser() {
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        myUserId = prefs.getString("userId", null);

        if (myUserId == null) {
            myUserId = "user_" + System.currentTimeMillis();
            prefs.edit().putString("userId", myUserId).apply();
        }
    }

    private void setupMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setMultiTouchControls(true);

        GeoPoint defaultPoint =
                new GeoPoint(28.6139, 77.2090);

        map.getController().setCenter(defaultPoint);

        map.getController().setZoom(5.0);
        routeLine = new Polyline();
        routeLine.setWidth(8f);
        map.getZoomController().setVisibility(
                CustomZoomButtonsController.Visibility.NEVER
        );
        markerManager =
                new MarkerManager(map, this);
        map.setMultiTouchControls(true);
        map.getOverlays().add(routeLine);
        map.addMapListener(new DelayedMapListener(new MapListener() {

            @Override
            public boolean onScroll(ScrollEvent event) {

                followUser = false;
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {

                followUser = false;
                return true;
            }

        }, 100));
    }

    private void setupViews() {
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnEditName = findViewById(R.id.btnEditName);
        txtAllowTracking = findViewById(R.id.txtAllowTracking);
        menuBtn = findViewById(R.id.menuBtn);
        searchIcon = findViewById(R.id.searchIcon);
        searchBox = findViewById(R.id.searchBox);
        btnStopTrackingRemote = findViewById(R.id.btnStopTrackingRemote);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnTrack = findViewById(R.id.btnTrack);
        btnShare = findViewById(R.id.btnShare);
        btnClearRoute = findViewById(R.id.btnClearRoute);
        btnDrawRoute = findViewById(R.id.btnDrawRoute);
        btnClearMarkers = findViewById(R.id.btnClearMarkers);
        btnAllowTracking = findViewById(R.id.btnAllowTracking);
        txtMovementInsight =findViewById(R.id.txtMovementInsight);
    }
    private void setupListeners() {
        btnStopTrackingRemote.setOnClickListener(v -> {

            if (currentlyTrackingId != null && trackingListener != null) {

                repository.removeListener(
                        currentlyTrackingId,
                        trackingListener
                );

                currentlyTrackingId = null;
                trackingListener = null;
            }

            viewModel.updateTrackedLocation(null);

            if (trackedMarker != null) {

                map.getOverlays().remove(trackedMarker);

                trackedMarker = null;
                movementAnalyzer.clear();

                txtMovementInsight.setText(
                        "No active tracking"
                );
            }

            map.invalidate();

            Toast.makeText(this,
                    "Stopped tracking",
                    Toast.LENGTH_SHORT).show();
        });
        btnZoomIn.setOnClickListener(v -> map.getController().zoomIn());
        btnEditName.setOnClickListener(v -> {
            showEditNameDialog();
        });
        btnShare.setOnClickListener(v -> {
            if (currentLocation == null) {

                Toast.makeText(this,
                        "Current location unavailable",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();

            String mapsLink =
                    "https://maps.google.com/?q=" + lat + "," + lon;

            String shareText =
                    "📍 My Current Location\n\n" + mapsLink;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(
                    shareIntent,
                    "Share Location"));
        });
        btnDrawRoute.setOnClickListener(v -> {
            if (currentLocation == null || searchedLocation == null) {

                Toast.makeText(this,
                        "Search a location first",
                        Toast.LENGTH_SHORT).show();

                return;
            }
            if (!NetworkUtils.isConnected(this)) {

                Toast.makeText(this,
                        "No internet connection",
                        Toast.LENGTH_SHORT).show();

                return;
            }
            drawVehicleRoute();
        });
        btnClearRoute.setOnClickListener(v -> {

            routeLine.setPoints(new ArrayList<>());

            map.invalidate();

            Toast.makeText(this,
                    "Route cleared",
                    Toast.LENGTH_SHORT).show();
        });
        btnClearMarkers.setOnClickListener(v -> {

            if (searchMarker != null) {
                map.getOverlays().remove(searchMarker);
                searchMarker = null;
            }

            if (trackedMarker != null) {
                map.getOverlays().remove(trackedMarker);
                trackedMarker = null;
                movementAnalyzer.clear();

                txtMovementInsight.setText(
                        "No active tracking"
                );
            }

            map.invalidate();

            Toast.makeText(this,
                    "Markers cleared",
                    Toast.LENGTH_SHORT).show();
        });
        btnZoomOut.setOnClickListener(v -> map.getController().zoomOut());
        btnTrack.setOnClickListener(v -> showTrackingDevices());
        btnAllowTracking.setOnClickListener(v -> toggleTracking());
        menuBtn.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));
        btnMyLocation.setOnClickListener(v -> {

            if(currentLocation != null)
            {
                map.getController().animateTo(currentLocation);
                map.getController().setZoom(17.0);
            }
        });
//        btnMyLocation.setOnClickListener(v -> {
//
//            LocationManager locationManager =
//                    (LocationManager)
//                            getSystemService(LOCATION_SERVICE);
//
//            boolean gpsEnabled =
//                    locationManager.isProviderEnabled(
//                            LocationManager.GPS_PROVIDER
//                    );
//
//            if (!gpsEnabled) {
//                Toast.makeText(this,
//                        "Turn on location services",
//                        Toast.LENGTH_SHORT).show();
//
//                return;
//            }
//
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED) {
//
//                return;
//            }
//
//            Toast.makeText(this,
//                    "Fetching location...",
//                    Toast.LENGTH_SHORT).show();
//
//            locationHelper.stop();
//
//            locationHelper.start(point -> {
//
//                viewModel.updateCurrentLocation(point);
//
//                runOnUiThread(() -> {
//
//                    currentLocation = point;
//
//
//
//                    map.getController().animateTo(point);
//
//                    map.getController().setZoom(17.0);
//                });
//            });
//        });
        searchBox.setOnEditorActionListener((v, actionId, event) -> {

            if (!NetworkUtils.isConnected(this)) {

                Toast.makeText(this,
                        "No internet connection",
                        Toast.LENGTH_SHORT).show();

                return true;
            }

            followUser = false;

            searchLocation();

            return true;
        });

        searchIcon.setOnClickListener(v -> {

            if (!NetworkUtils.isConnected(this)) {

                Toast.makeText(this,
                        "No internet connection",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            followUser = false;

            searchLocation();
        });
    }
    // ================= TRACKING =================
    private void toggleTracking() {
        if (currentLocation == null) {

            Toast.makeText(this,
                    "Turn on location services",
                    Toast.LENGTH_SHORT).show();

            return;
        }
        viewModel.toggleTracking();

        boolean state = Boolean.TRUE.equals(viewModel.getTrackingState().getValue());

        repository.setTracking(state);
        Intent serviceIntent =
                new Intent(
                        MainActivity.this,
                        com.example.maptrackapplication.tracking.TrackingService.class
                );

        if (state) {

            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.O) {

                startForegroundService(serviceIntent);

            } else {

                startService(serviceIntent);
            }

        } else {

            stopService(serviceIntent);
        }
        SharedPreferences prefs =
                getSharedPreferences("MyApp", MODE_PRIVATE);

        prefs.edit()
                .putBoolean("tracking_enabled", state)
                .apply();
        txtAllowTracking.setText(
                state ? "Stop Sharing Live Location" : "Start Sharing Live Location"
        );
    }
    private void showEditNameDialog() {

        EditText input = new EditText(this);

        input.setHint("Enter device name");

        new AlertDialog.Builder(this)
                .setTitle("Edit Device Name")
                .setView(input)

                .setPositiveButton("Save",
                        (dialog, which) -> {

                            String name =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (!name.isEmpty()) {

                                repository.setName(name);

                                Toast.makeText(
                                        this,
                                        "Device name updated",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        })

                .setNegativeButton("Cancel", null)

                .show();
    }

    // ================= DEVICE TRACK =================

    private void trackDevice(String userId) {

        if (currentlyTrackingId != null && trackingListener != null) {
            repository.removeListener(currentlyTrackingId, trackingListener);
        }

        currentlyTrackingId = userId;
        firstTrackedFix = true;
        trackingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Double lat = snapshot.child("latitude").getValue(Double.class);
                Double lon = snapshot.child("longitude").getValue(Double.class);
                Boolean tracking = snapshot.child("tracking").getValue(Boolean.class);
                Long lastUpdated =
                        snapshot.child("lastUpdated")
                                .getValue(Long.class);

                if (tracking == null || !tracking) {

                    movementAnalyzer.clear();

                    String lastSeen = "Unknown";

                    if (lastUpdated != null) {

                        long seconds =
                                (System.currentTimeMillis()
                                        - lastUpdated) / 1000;

                        lastSeen =
                                seconds + " sec ago";
                    }

                    txtMovementInsight.setText(
                            "Offline • Last updated "
                                    + lastSeen
                    );
                    if (trackedMarker != null) {
                        trackedMarker.setAlpha(0.5f);
                    }
                    return;
                }

                if (lat != null && lon != null) {

                    if (trackedMarker != null) {
                        trackedMarker.setAlpha(1.0f);
                    }

                    viewModel.updateTrackedLocation(
                            new GeoPoint(lat, lon)
                    );
                }
            }

            @Override public void onCancelled(DatabaseError error) {}
        };

        repository.listenToUser(userId, trackingListener);
    }
    // ================= SEARCH =================
    private void showTrackingDevices() {

        repository.getRef().addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        ArrayList<String> deviceNames = new ArrayList<>();
                        ArrayList<String> deviceIds = new ArrayList<>();

                        for (DataSnapshot userSnap : snapshot.getChildren()) {

                            String userId = userSnap.getKey();

                            String name = userSnap.child("name")
                                    .getValue(String.class);

                            Boolean tracking = userSnap.child("tracking")
                                    .getValue(Boolean.class);

                            Boolean online = userSnap.child("online")
                                    .getValue(Boolean.class);

                            if (userId == null || userId.equals(myUserId))
                                continue;

                            if (tracking != null && tracking) {

                                String displayName;

                                if (name != null && !name.isEmpty()) {
                                    displayName = name;
                                } else {
                                    displayName = userId;
                                }

                                if (online != null && online) {
                                    displayName += " 🟢";
                                } else {
                                    displayName += " ⚫";
                                }

                                deviceNames.add(displayName);
                                deviceIds.add(userId);
                            }
                        }

                        if (deviceNames.isEmpty()) {

                            Toast.makeText(MainActivity.this,
                                    "No active devices found",
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("Track Device");

                        builder.setItems(
                                deviceNames.toArray(new String[0]),
                                (dialog, which) -> {

                                    String selectedUserId = deviceIds.get(which);

                                    trackDevice(selectedUserId);

                                    Toast.makeText(MainActivity.this,
                                            "Tracking started",
                                            Toast.LENGTH_SHORT).show();

                                    drawerLayout.closeDrawer(GravityCompat.START);
                                });

                        builder.show();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(MainActivity.this,
                                "Failed to load devices",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void searchLocation() {
        String location = searchBox.getText().toString().trim();
        if (location.isEmpty()) return;
        new Thread(() -> {
            try {
                SearchService searchService =
                        new SearchService();
                GeoPoint dest =
                        searchService.searchLocation(location);
                if (dest == null) {
                    runOnUiThread(() ->
                            Toast.makeText(
                                    MainActivity.this,
                                    "Location not found",
                                    Toast.LENGTH_SHORT
                            ).show());

                    return;
                }
                runOnUiThread(() -> {
                    searchedLocation = dest;
                    if (searchMarker != null) {
                        map.getOverlays().remove(searchMarker);
                    }
                    searchMarker =
                            markerManager.createSearchMarker(dest);
                    map.getController().animateTo(dest);
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "Search failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
                e.printStackTrace();
            }
        }).start();
    }
    private void drawVehicleRoute() {

        new Thread(() -> {

            try {

                RouteService routeService =
                        new RouteService();

                ArrayList<GeoPoint> points =
                        routeService.fetchRoute(
                                currentLocation,
                                searchedLocation
                        );

                runOnUiThread(() -> {
                    routeLine.setPoints(points);
                    map.invalidate();

                    Toast.makeText(this,
                            "Vehicle route drawn",
                            Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Route failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());

                e.printStackTrace();
            }

        }).start();
    }
    // ================= LIFECYCLE =================

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            if (locationHelper == null) {
                locationHelper = new LocationHelper(this);
            }

            locationHelper.stop();

            locationHelper.start(
                    point -> viewModel.updateCurrentLocation(point)
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentlyTrackingId != null && trackingListener != null) {
            repository.removeListener(currentlyTrackingId, trackingListener);
        }
    }

}