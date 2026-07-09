package com.example.maptrackapplication;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.location.*;
import org.osmdroid.util.GeoPoint;

public class LocationHelper {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public interface LocationListener {
        void onLocationChanged(GeoPoint point);
    }

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void start(LocationListener listener) {

        LocationRequest request = LocationRequest.create();
        request.setInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || result.getLastLocation() == null) return;

                double lat = result.getLastLocation().getLatitude();
                double lon = result.getLastLocation().getLongitude();

                listener.onLocationChanged(new GeoPoint(lat, lon));
            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
    }

    public void stop() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}