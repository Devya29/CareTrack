package com.example.maptrackapplication.model;

public class LocationPoint {

    private final double latitude;
    private final double longitude;
    private final long timestamp;

    public LocationPoint(
            double latitude,
            double longitude,
            long timestamp
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }
}