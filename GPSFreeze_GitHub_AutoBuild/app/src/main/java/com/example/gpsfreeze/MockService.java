package com.example.gpsfreeze;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

public class MockService extends Service {
    // 25 November 2026, 00:00 India Standard Time.
    private static final long EXPIRY_TIME_MILLIS = 1795545000000L;

    static boolean isExpired() {
        return System.currentTimeMillis() >= EXPIRY_TIME_MILLIS;
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocationManager manager;
    private double lat, lon;
    private boolean active;

    private final String[] providers = {
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
    };

    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (!active) return;
            if (isExpired()) {
                shutdown();
                stopSelf();
                return;
            }

            for (String provider : providers) {
                try {
                    Location location = new Location(provider);
                    location.setLatitude(lat);
                    location.setLongitude(lon);
                    location.setAccuracy(3f);
                    location.setAltitude(0);
                    location.setTime(System.currentTimeMillis());
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    location.setSpeed(0);
                    location.setBearing(0);
                    manager.setTestProviderLocation(provider, location);
                } catch (Exception ignored) {
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                "freeze", "GPS Freeze", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        if ("STOP".equals(intent.getAction())) {
            shutdown();
            stopSelf();
            return START_NOT_STICKY;
        }

        lat = intent.getDoubleExtra("lat", 0);
        lon = intent.getDoubleExtra("lon", 0);
        Notification notification = new Notification.Builder(this, "freeze")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("GPS Freeze active")
                .setContentText("Mock coordinates: " + lat + ", " + lon)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }

        if (isExpired()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            for (String provider : providers) {
                try {
                    manager.addTestProvider(
                            provider, false, false, false, false, true, true, true, 1, 1);
                } catch (IllegalArgumentException alreadyExists) {
                }
                manager.setTestProviderEnabled(provider, true);
            }
            active = true;
            handler.removeCallbacks(updater);
            handler.post(updater);
        } catch (SecurityException e) {
            Toast.makeText(this, "Choose GPS Freeze as mock location app in Developer options",
                    Toast.LENGTH_LONG).show();
            shutdown();
            stopSelf();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to start: " + e.getMessage(), Toast.LENGTH_LONG).show();
            shutdown();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void shutdown() {
        active = false;
        handler.removeCallbacks(updater);
        if (manager != null) {
            for (String provider : providers) {
                try {
                    manager.removeTestProvider(provider);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
