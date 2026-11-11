package com.example.gpsfreeze;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String EXPIRY_MESSAGE =
            "GPS Freeze expired on 25 November 2026 (IST).";

    private EditText lat, lon;
    private TextView status;
    private Button startButton;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);

        TextView title = new TextView(this);
        title.setText("GPS Freeze — mock location");
        title.setTextSize(23);
        root.addView(title);

        TextView hint = new TextView(this);
        hint.setText("Select GPS Freeze in Developer options → Select mock location app. "
                + "Then choose coordinates and press Start. Map requires internet.");
        root.addView(hint);

        Button settings = new Button(this);
        settings.setText("Open Developer Options");
        settings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            } catch (Exception e) {
                toast("Open Developer options manually in Settings");
            }
        });
        root.addView(settings);

        lat = new EditText(this);
        lat.setHint("Latitude (-90 to 90)");
        lat.setInputType(8194);
        lat.setSingleLine(true);
        root.addView(lat);

        lon = new EditText(this);
        lon.setHint("Longitude (-180 to 180)");
        lon.setInputType(8194);
        lon.setSingleLine(true);
        root.addView(lon);

        SharedPreferences preferences = getSharedPreferences("location", MODE_PRIVATE);
        lat.setText(preferences.getString("lat", "29.8010"));
        lon.setText(preferences.getString("lon", "76.3990"));

        LinearLayout buttons = new LinearLayout(this);
        startButton = new Button(this);
        startButton.setText("START FREEZE");
        buttons.addView(startButton, new LinearLayout.LayoutParams(0, -2, 1));

        Button stop = new Button(this);
        stop.setText("STOP");
        buttons.addView(stop, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(buttons);

        status = new TextView(this);
        status.setText("Status: idle");
        root.addView(status);

        WebView map = new WebView(this);
        map.getSettings().setJavaScriptEnabled(true);
        map.getSettings().setDomStorageEnabled(true);
        map.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void picked(String latitude, String longitude) {
                runOnUiThread(() -> {
                    lat.setText(latitude);
                    lon.setText(longitude);
                });
            }
        }, "Picker");
        map.setWebViewClient(new WebViewClient());
        map.loadUrl("file:///android_asset/map.html");
        root.addView(map, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        startButton.setOnClickListener(v -> {
            if (showExpiryIfNeeded()) return;
            try {
                double latitude = Double.parseDouble(lat.getText().toString().trim());
                double longitude = Double.parseDouble(lon.getText().toString().trim());
                if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                        || latitude < -90 || latitude > 90
                        || longitude < -180 || longitude > 180) {
                    toast("Invalid coordinates");
                    return;
                }

                preferences.edit()
                        .putString("lat", String.valueOf(latitude))
                        .putString("lon", String.valueOf(longitude))
                        .apply();

                if (Build.VERSION.SDK_INT >= 23
                        && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1);
                    toast("Grant location permission, then press Start again");
                    return;
                }

                Intent intent = new Intent(this, MockService.class);
                intent.setAction("START");
                intent.putExtra("lat", latitude);
                intent.putExtra("lon", longitude);
                startForegroundService(intent);
                status.setText("Starting mock location. Check notification for errors.");
            } catch (NumberFormatException e) {
                toast("Enter valid latitude and longitude");
            }
        });

        stop.setOnClickListener(v -> {
            if (showExpiryIfNeeded()) return;
            Intent intent = new Intent(this, MockService.class);
            intent.setAction("STOP");
            startService(intent);
            status.setText("Status: stopped");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showExpiryIfNeeded();
    }

    private boolean showExpiryIfNeeded() {
        if (!MockService.isExpired()) return false;
        stopService(new Intent(this, MockService.class));
        startButton.setEnabled(false);
        status.setText(EXPIRY_MESSAGE);
        return true;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
