package brice.explorun.observables;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import brice.explorun.fragments.MapsFragment;
import brice.explorun.models.Observable;

public class LocationManager extends Observable implements LocationListener
{
	private final int REQUEST_CHECK_SETTINGS = 0x1;

	private MapsFragment mapFragment;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation = null;

	private final String LOCATION_KEY = "location";
	private final int refreshInterval = 10000; // Position refresh interval (in ms)

	public LocationManager(MapsFragment mapFragment, GoogleApiClient googleApiClient)
	{
		this.mapFragment = mapFragment;
		this.mGoogleApiClient = googleApiClient;
		this.registerObserver(mapFragment);
	}

	public Location getLastLocation() {
		return mLastLocation;
	}

	public void setLastLocation(Location loc){
		this.mLastLocation = loc;
	}

	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putParcelable(LOCATION_KEY, this.mLastLocation);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.d("onLocationChanged", location.toString());
		this.mLastLocation = location;
		this.notifyAllObservers();
	}

	public void initLocation()
	{
		if (ActivityCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
			if (this.mLastLocation != null)
			{
				this.notifyAllObservers();
			}
			else
			{
				mapFragment.restoreLastLocation();
			}
			startLocationUpdates();
		}
	}

	public void updateLocationFromPreferences()
	{
		SharedPreferences sharedPref = this.mapFragment.getActivity().getPreferences(Context.MODE_PRIVATE);
		float latitude = sharedPref.getFloat("latitude", -1);
		float longitude = sharedPref.getFloat("longitude", -1);
		if(latitude != -1 && longitude != -1)
		{
			mLastLocation = new Location("");
			mLastLocation.setLatitude(latitude);
			mLastLocation.setLongitude(longitude);
		}
	}

	public void storeLastLocation(LatLng loc)
	{
		SharedPreferences sharedPref = this.mapFragment.getActivity().getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putFloat("latitude", (float) loc.latitude);
		editor.putFloat("longitude", (float) loc.longitude);
		editor.apply();
	}

	public void checkLocationEnabled()
	{
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(createLocationRequest());

		PendingResult<LocationSettingsResult> result =
				LocationServices.SettingsApi.checkLocationSettings(this.mGoogleApiClient, builder.build());

		result.setResultCallback(new ResultCallback<LocationSettingsResult>()
		{
			@Override
			public void onResult(LocationSettingsResult locationSettingsResult)
			{

				final Status status = locationSettingsResult.getStatus();
				switch (status.getStatusCode())
				{
					case LocationSettingsStatusCodes.SUCCESS:
						// All location settings are satisfied. The client can initialize location requests here.
						initLocation();
						break;

					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						// Location settings are not satisfied. But could be fixed by showing the user a dialog.
						try {
							// Show the dialog by calling startResolutionForResult() and check the result in onActivityResult().
							status.startResolutionForResult(mapFragment.getActivity(), REQUEST_CHECK_SETTINGS);
						}
						catch (IntentSender.SendIntentException e) {
							// Ignore the error.
						}
						break;

					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						// Location settings are not satisfied.
						break;
				}
			}
		});
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
		if (ActivityCompat.checkSelfPermission(this.mapFragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, createLocationRequest(), this);
		}
	}

	public void stopLocationUpdates()
	{
		LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
	}
}
