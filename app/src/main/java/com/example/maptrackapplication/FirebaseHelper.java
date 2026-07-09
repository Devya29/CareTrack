package com.example.maptrackapplication;

import com.google.firebase.database.*;

public class FirebaseHelper {

    private DatabaseReference firebaseRef;
    private String userId;

    public FirebaseHelper(String userId) {
        this.userId = userId;

        firebaseRef = FirebaseDatabase.getInstance(
                "YOUR-FIREBASE-APP-URL"
        ).getReference("users");

        firebaseRef.child(userId).child("online").setValue(true);
        firebaseRef.child(userId).child("online").onDisconnect().setValue(false);
    }

    public void updateLocation(double lat, double lon) {
        firebaseRef.child(userId).child("latitude").setValue(lat);
        firebaseRef.child(userId).child("longitude").setValue(lon);
        firebaseRef.child(userId)
                .child("lastUpdated")
                .setValue(System.currentTimeMillis());
    }

    public void removeListener(String id, ValueEventListener listener) {
        firebaseRef.child(id).removeEventListener(listener);
    }

    public void addListener(String id, ValueEventListener listener) {
        firebaseRef.child(id).addValueEventListener(listener);
    }

    public DatabaseReference getRef() {
        return firebaseRef;
    }

    public void setTracking(boolean value) {
        firebaseRef.child(userId).child("tracking").setValue(value);
    }

    public void setName(String name) {
        firebaseRef.child(userId).child("name").setValue(name);
    }
}