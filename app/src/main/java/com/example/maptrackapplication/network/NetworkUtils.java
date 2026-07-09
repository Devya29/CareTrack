package com.example.maptrackapplication.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    public static boolean isConnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)
                        context.getSystemService(
                                Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return false;

        NetworkCapabilities capabilities =
                cm.getNetworkCapabilities(
                        cm.getActiveNetwork());

        return capabilities != null
                && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}