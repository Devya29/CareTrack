package com.example.maptrackapplication.network;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteService {
    private final OkHttpClient httpClient =
            new OkHttpClient();
    public ArrayList<GeoPoint> fetchRoute(
            GeoPoint start,
            GeoPoint end
    ) throws Exception {

        ArrayList<GeoPoint> routePoints =
                new ArrayList<>();

        String url =
                "https://router.project-osrm.org/route/v1/driving/"
                        + start.getLongitude() + ","
                        + start.getLatitude() + ";"
                        + end.getLongitude() + ","
                        + end.getLatitude()
                        + "?overview=full&geometries=geojson";
        Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .header("User-Agent", "MapTrackApp")
                        .build()
        ).execute();
        String json = response.body().string();
        JSONObject obj = new JSONObject(json);
        JSONArray routes =
                obj.getJSONArray("routes");
        JSONObject route =
                routes.getJSONObject(0);
        JSONObject geometry =
                route.getJSONObject("geometry");
        JSONArray coordinates =
                geometry.getJSONArray("coordinates");
        for (int i = 0; i < coordinates.length(); i++) {
            JSONArray point =
                    coordinates.getJSONArray(i);
            double lon = point.getDouble(0);

            double lat = point.getDouble(1);

            routePoints.add(
                    new GeoPoint(lat, lon)
            );
        }
        return routePoints;
    }
}
