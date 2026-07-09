package com.example.maptrackapplication;

import com.google.firebase.database.*;

import org.osmdroid.util.GeoPoint;

public class AppRepository {

    private final FirebaseHelper firebaseHelper;

    public AppRepository(String userId) {
        firebaseHelper = new FirebaseHelper(userId);
    }

    public void updateLocation(GeoPoint point) {
        firebaseHelper.updateLocation(point.getLatitude(), point.getLongitude());
    }

    public void setTracking(boolean value) {
        firebaseHelper.setTracking(value);
    }

    public void listenToUser(String userId, ValueEventListener listener) {
        firebaseHelper.addListener(userId, listener);
    }

    public void removeListener(String userId, ValueEventListener listener) {
        firebaseHelper.removeListener(userId, listener);
    }
    public DatabaseReference getRef() {
        return firebaseHelper.getRef();
    }

    public void setName(String name) {
        firebaseHelper.setName(name);
    }
}