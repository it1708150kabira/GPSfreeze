package com.example.gpsfreeze;

import android.app.*;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.*;
import android.content.pm.ServiceInfo;
import android.widget.Toast;

public class MockService extends Service {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocationManager manager;
    private double lat, lon;
    private boolean active=false;
    private final String[] providers = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};
    private final Runnable updater = new Runnable(){@Override public void run(){
        if (!active) return;
        for (String provider:providers) try {
            Location loc=new Location(provider);loc.setLatitude(lat);loc.setLongitude(lon);
            loc.setAccuracy(3f);loc.setAltitude(0);loc.setTime(System.currentTimeMillis());
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            loc.setSpeed(0);loc.setBearing(0);
            manager.setTestProviderLocation(provider,loc);
        } catch(Exception ignored){}
        handler.postDelayed(this,1000);
    }};
    @Override public void onCreate(){super.onCreate();manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        NotificationChannel ch=new NotificationChannel("freeze","GPS Freeze",NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null)return START_NOT_STICKY;
        if("STOP".equals(intent.getAction())){shutdown();stopSelf();return START_NOT_STICKY;}
        lat=intent.getDoubleExtra("lat",0);lon=intent.getDoubleExtra("lon",0);
        Notification notification=new Notification.Builder(this,"freeze").setSmallIcon(android.R.drawable.ic_menu_mylocation)
          .setContentTitle("GPS Freeze active").setContentText("Mock coordinates: "+lat+", "+lon)
          .setOngoing(true).build();
        if(Build.VERSION.SDK_INT>=29) startForeground(1,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else startForeground(1,notification);
        try {
            for(String provider:providers){
                try {manager.addTestProvider(provider,false,false,false,false,true,true,true,1,1);} catch(IllegalArgumentException alreadyExists){}
                manager.setTestProviderEnabled(provider,true);
            }
            active=true;handler.removeCallbacks(updater);handler.post(updater);
        }catch(SecurityException e){Toast.makeText(this,"Choose GPS Freeze as mock location app in Developer options",Toast.LENGTH_LONG).show();shutdown();stopSelf();}
        catch(Exception e){Toast.makeText(this,"Unable to start: "+e.getMessage(),Toast.LENGTH_LONG).show();shutdown();stopSelf();}
        return START_NOT_STICKY;
    }
    private void shutdown(){active=false;handler.removeCallbacks(updater);
        if(manager!=null)for(String provider:providers)try{manager.removeTestProvider(provider);}catch(Exception ignored){}
    }
    @Override public void onDestroy(){shutdown();super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
