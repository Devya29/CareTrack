package com.example.maptrackapplication.tracking;
import com.example.maptrackapplication.AppRepository;
import com.example.maptrackapplication.LocationHelper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;



public class TrackingService extends Service {

    private LocationHelper locationHelper;
    private AppRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new NotificationCompat.Builder(this, "tracking_channel")
                        .setContentTitle("Live Tracking Active")
                        .setContentText("Location sharing is running")
                        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                        .build();

        startForeground(1, notification);

        SharedPreferences prefs =
                getSharedPreferences("MyApp", MODE_PRIVATE);

        String userId =
                prefs.getString("userId", null);

        repository = new AppRepository(userId);

        locationHelper = new LocationHelper(this);

        locationHelper.start(point -> {

            repository.updateLocation(point);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationHelper != null) {
            locationHelper.stop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "tracking_channel",
                            "Tracking Service",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
}