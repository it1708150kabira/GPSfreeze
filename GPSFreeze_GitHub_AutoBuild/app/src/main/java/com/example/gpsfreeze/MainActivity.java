package com.example.gpsfreeze;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

public class MainActivity extends Activity {
    EditText lat, lon;
    TextView status;
    WebView map;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this); title.setText("GPS Freeze — mock location"); title.setTextSize(23); root.addView(title);
        TextView hint = new TextView(this); hint.setText("Select GPS Freeze in Developer options → Select mock location app. Then choose coordinates and press Start. Map requires internet."); root.addView(hint);
        Button settings = new Button(this); settings.setText("Open Developer Options"); settings.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); } catch(Exception e) { toast("Open Developer options manually in Settings"); } }); root.addView(settings);
        lat = new EditText(this); lat.setHint("Latitude (-90 to 90)"); lat.setInputType(8194); lat.setSingleLine(true); root.addView(lat);
        lon = new EditText(this); lon.setHint("Longitude (-180 to 180)"); lon.setInputType(8194); lon.setSingleLine(true); root.addView(lon);
        android.content.SharedPreferences p = getSharedPreferences("location", MODE_PRIVATE);
        lat.setText(p.getString("lat", "29.8010")); lon.setText(p.getString("lon", "76.3990"));
        LinearLayout buttons = new LinearLayout(this);
        Button start = new Button(this); start.setText("START FREEZE"); buttons.addView(start, new LinearLayout.LayoutParams(0,-2,1));
        Button stop = new Button(this); stop.setText("STOP"); buttons.addView(stop, new LinearLayout.LayoutParams(0,-2,1)); root.addView(buttons);
        status = new TextView(this); status.setText("Status: idle"); root.addView(status);
        map = new WebView(this); map.getSettings().setJavaScriptEnabled(true); map.getSettings().setDomStorageEnabled(true);
        map.addJavascriptInterface(new Object() { @JavascriptInterface public void picked(String a, String b) { runOnUiThread(() -> {lat.setText(a); lon.setText(b);}); } }, "Picker");
        map.setWebViewClient(new WebViewClient()); map.loadUrl("file:///android_asset/map.html");
        root.addView(map, new LinearLayout.LayoutParams(-1, 0, 1)); setContentView(root);
        start.setOnClickListener(v -> {
            try {
                double a=Double.parseDouble(lat.getText().toString().trim()), b=Double.parseDouble(lon.getText().toString().trim());
                if (!Double.isFinite(a)||!Double.isFinite(b)||a < -90||a > 90||b < -180||b > 180) {toast("Invalid coordinates");return;}
                p.edit().putString("lat", String.valueOf(a)).putString("lon", String.valueOf(b)).apply();
                if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                    toast("Grant location permission, then press Start again");return;
                }
                Intent i = new Intent(this, MockService.class); i.setAction("START");i.putExtra("lat",a);i.putExtra("lon",b);
                startForegroundService(i);status.setText("Starting mock location. Check notification for errors.");
            } catch(NumberFormatException e) {toast("Enter valid latitude and longitude");}
        });
        stop.setOnClickListener(v -> {Intent i = new Intent(this,MockService.class);i.setAction("STOP");startService(i);status.setText("Status: stopped");});
    }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
