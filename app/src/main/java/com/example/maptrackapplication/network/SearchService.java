package com.example.maptrackapplication.network;

import org.json.JSONArray;
import org.osmdroid.util.GeoPoint;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchService {

    private final OkHttpClient httpClient = new OkHttpClient();

    public GeoPoint searchLocation(String location) throws Exception {

        String url =
                "https://nominatim.openstreetmap.org/search?q="
                        + location
                        + "&format=json&limit=1";

        Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .header("User-Agent", "MapTrackApp")
                        .build()
        ).execute();

        JSONArray arr =
                new JSONArray(response.body().string());

        if (arr.length() > 0) {

            double lat =
                    arr.getJSONObject(0).getDouble("lat");

            double lon =
                    arr.getJSONObject(0).getDouble("lon");

            return new GeoPoint(lat, lon);
        }

        return null;
    }
}