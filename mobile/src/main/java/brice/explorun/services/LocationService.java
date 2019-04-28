package brice.explorun.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import brice.explorun.R;
import brice.explorun.activities.MainActivity;

public class LocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static boolean isStarted = false;
    public static GoogleApiClient mGoogleApiClient = null;
    final int notificationId = 67;
    final String notificationChannel = "locChannel";
    final String notificationChannelName = "Localisation service";
    Intent locationBroadcastIntent = new Intent("ex_location");

    final static int refreshInterval = 10000; // Position refresh interval (in ms)

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel chan = new NotificationChannel(notificationChannel, notificationChannelName, NotificationManager.IMPORTANCE_NONE);
			chan.setLightColor(Color.BLUE);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);
		}

        Notification notification = new NotificationCompat.Builder(this, notificationChannel)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build();

        startForeground(notificationId, notification);
        Log.i("explorun_location", "Service successfully started");

		// Connection to Google API
		try{
			mGoogleApiClient.connect();
		}
		catch(Exception ex){
			Log.e("explorun_location", "Can't connect to Google API Client: " + ex.getMessage());
		}
    }

	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i)
	{

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
	}

	@Override
    public void onDestroy() {
		Log.i("explorun_location","Stopping location service");
        isStarted = false;
        if (mGoogleApiClient.isConnected())
		{
			stopLocationUpdates();
			mGoogleApiClient.disconnect();
		}
        super.onDestroy();
    }

    public static LocationRequest createLocationRequest()
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
        else if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
        	Log.i("explorun_location", "Requesting location updates");
        	storeLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
        }
    }

    public void stopLocationUpdates()
    {
    	if (mGoogleApiClient.isConnected())
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(final Location loc) {
        Log.i("explorun_location", "Location changed");
        storeLastLocation(loc);
    }

    public void storeLastLocation(final Location loc)
	{
		if (loc != null)
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putFloat("latitude", (float) loc.getLatitude());
			editor.putFloat("longitude", (float) loc.getLongitude());
			editor.apply();
			sendBroadcast(locationBroadcastIntent);
		}
	}
}
