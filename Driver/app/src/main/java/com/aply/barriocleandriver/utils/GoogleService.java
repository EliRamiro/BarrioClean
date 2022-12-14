package com.aply.barriocleandriver.utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

public class GoogleService extends Service implements LocationListener {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private final Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 15000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;
    Credentials cred;
    Context ctx;

    public GoogleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        cred = new Credentials(this);
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
        cred.saveData(Preference.LOCATION_SERVICE, "1");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        new Utils(ctx).updateLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {
            new Utils().showToast(ctx, "No se pudo obtener su ubicaci??n", 2);
        } else {
            if (isNetworkEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {

                        Log.e("network_latitud", location.getLatitude() + "");
                        Log.e("network_longitud", location.getLongitude() + "");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        cred.saveData(Preference.LATITUD, latitude + "");
                        cred.saveData(Preference.LONGITUD, longitude + "");

                        fn_update(location);
                    }
                }
            }
            if (isGPSEnable) {
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        Log.e("gps_latitud", location.getLatitude() + "");
                        Log.e("gps_longitud", location.getLongitude() + "");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        cred.saveData(Preference.LATITUD, latitude + "");
                        cred.saveData(Preference.LONGITUD, longitude + "");

                        fn_update(location);
                    }
                }
            }
        }
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    private void fn_update(Location location) {
        intent.putExtra(Preference.LATITUD, location.getLatitude() + "");
        intent.putExtra(Preference.LONGITUD, location.getLongitude() + "");
        cred.saveData(Preference.LATITUD, location.getLatitude() + "");
        cred.saveData(Preference.LONGITUD, location.getLongitude() + "");
        sendBroadcast(intent);
    }
}
