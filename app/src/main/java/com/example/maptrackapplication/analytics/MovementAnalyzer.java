package com.example.maptrackapplication.analytics;

import com.example.maptrackapplication.model.LocationPoint;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MovementAnalyzer {

    private final List<LocationPoint> history =
            new ArrayList<>();

    public void addPoint(GeoPoint point) {

        history.add(
                new LocationPoint(
                        point.getLatitude(),
                        point.getLongitude(),
                        System.currentTimeMillis()
                )
        );

        if (history.size() > 10) {
            history.remove(0);
        }
    }

    public String generateInsight() {

        if (history.size() < 2) {
            return "Collecting movement data...";
        }

        LocationPoint latest =
                history.get(history.size() - 1);

        LocationPoint previous =
                history.get(history.size() - 2);
        float[] result = new float[1];

        android.location.Location.distanceBetween(
                previous.getLatitude(),
                previous.getLongitude(),
                latest.getLatitude(),
                latest.getLongitude(),
                result
        );

        double distanceMeters = result[0];

        long timeDiffMillis =
                latest.getTimestamp()
                        - previous.getTimestamp();

        double seconds = timeDiffMillis / 1000.0;

        if (seconds <= 0) {
            return "Analyzing movement...";
        }

        double speedMps = distanceMeters / seconds;

        double speedKmph = speedMps * 3.6;

        if (speedKmph < 2) {
            return "Device appears stationary";
        }

        if (speedKmph < 8) {
            return "Device likely moving on foot";
        }

        if (speedKmph < 25) {
            return "Device likely travelling slowly";
        }

        return "Device likely travelling by vehicle";
    }
    public void clear() {
        history.clear();
    }
}