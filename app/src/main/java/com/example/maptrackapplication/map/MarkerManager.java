package com.example.maptrackapplication.map;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.maptrackapplication.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MarkerManager {

    private final MapView map;
    private final Context context;

    public MarkerManager(MapView map, Context context) {
        this.map = map;
        this.context = context;
    }

    public Marker createSearchMarker(
            GeoPoint point
    ) {

        Marker marker = new Marker(map);

        marker.setIcon(
                ContextCompat.getDrawable(
                        context,
                        R.drawable.destination_marker
                )
        );

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        marker.setPosition(point);

        map.getOverlays().add(marker);

        return marker;
    }
    public Marker createTrackedMarker(
            GeoPoint point
    ) {

        Marker marker = new Marker(map);

        marker.setIcon(
                ContextCompat.getDrawable(
                        context,
                        R.drawable.tracked_device
                )
        );

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );
        marker.setPosition(point);
        map.getOverlays().add(marker);

        return marker;
    }
    public Marker createLiveMarker(
            GeoPoint point
    ) {

        Marker marker = new Marker(map);

        marker.setIcon(
                ContextCompat.getDrawable(
                        context,
                        R.drawable.current_location
                )
        );

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_CENTER
        );

        marker.setPosition(point);

        map.getOverlays().add(marker);

        return marker;
    }
}