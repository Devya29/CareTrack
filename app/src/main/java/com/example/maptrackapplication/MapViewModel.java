package com.example.maptrackapplication;

import androidx.lifecycle.LiveData; // observable read only data
import androidx.lifecycle.MutableLiveData; // observable mutable data
import androidx.lifecycle.ViewModel;  //holds the state of the application

import org.osmdroid.util.GeoPoint; //OSMdriod's coordinates class stores latitude + longitude

public class MapViewModel extends ViewModel {

    private final MutableLiveData<GeoPoint> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<GeoPoint> trackedLocation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);

    public LiveData<GeoPoint> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<GeoPoint> getTrackedLocation() {
        return trackedLocation;
    }

    public LiveData<Boolean> getTrackingState() {
        return isTracking;
    }

    public void updateCurrentLocation(GeoPoint point) {
        currentLocation.setValue(point);
    }

    public void updateTrackedLocation(GeoPoint point) {
        trackedLocation.setValue(point);
    }

    public void toggleTracking() {
        Boolean val = isTracking.getValue();
        isTracking.setValue(val == null || !val);
    }
}