package brice.explorun.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import brice.explorun.R;

/**
 * Created by germain on 11/6/17.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static boolean isStarted = false;
    public static GoogleApiClient mGoogleApiClient = null;
    final int notificationId = 67;
    final String notificationChannel = "locChannel";
    Intent intent;

    final int refreshInterval = 10000; // Position refresh interval (in ms)


    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        try{
            mGoogleApiClient.connect();
        }
        catch(Exception ex){
            Log.e("eX_location", "Google api client not properly connected, trying again later");
        }

        Intent notificationIntent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, notificationChannel)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_photo_camera)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build();

        startForeground(notificationId, notification);
        intent = new Intent("ex_location");
        startLocationUpdates();
        Log.i("explorun_location", "Service successfully started");
    }


    @Override
    public void onDestroy() {
        isStarted = false;
        stopLocationUpdates();
        super.onDestroy();
    }

    private LocationRequest createLocationRequest()
    {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(refreshInterval);
        mLocationRequest.setFastestInterval(refreshInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void startLocationUpdates()
    {
        if(!mGoogleApiClient.isConnected()){
            Log.e("eX_location","Can't start location update, because not connected to google api");
            Handler h = new Handler();
            Runnable r = new Runnable()
            {
                @Override
                public void run() {
                    try{
                        mGoogleApiClient.connect();
                    }
                    catch(Exception ex){
                        Log.e("eX_location", "Google api client not properly connected, trying again later");
                    }
                    startLocationUpdates();
                }
            };
            h.postDelayed(r, 5000);
        }
        else if (ActivityCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, createLocationRequest(), this);
        }
    }

    public void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(final Location loc) {
        Log.i("explorun_location", "Location changed");
        loc.getLatitude();
        loc.getLongitude();
        intent.putExtra("latitude", loc.getLatitude());
        intent.putExtra("longitude", loc.getLongitude());
        sendBroadcast(intent);
    }
}
