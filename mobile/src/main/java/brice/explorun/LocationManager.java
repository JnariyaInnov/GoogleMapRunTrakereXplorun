package brice.explorun;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationManager implements LocationListener
{
	private final int REQUEST_CHECK_SETTINGS = 0x1;

	private Activity context;
	private GoogleMap mMap;
	private GoogleApiClient mGoogleApiClient;

	private boolean isFirstRequest = true;
	private boolean mRequestingLocationUpdates = false;
	private Location mLastLocation = null;
	private MarkerOptions userLocationMarkerOptions;
	private Marker userMarker;

	private final String REQUESTING_LOCATION_UPDATES_KEY = "requesting";
	private final String FIRST_REQUEST_KEY = "isFirstRequest";
	private final String LOCATION_KEY = "location";
	private final int refreshInterval = 10000; // Intervalle de rafraîchissement de la position (en ms)

	LocationManager(Activity context, GoogleApiClient googleApiClient)
	{
		this.context = context;
		this.mMap = null;
		this.mGoogleApiClient = googleApiClient;
	}

	void setMap(GoogleMap map)
	{
		this.mMap = map;
		if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			this.mMap.setMyLocationEnabled(true);
			this.mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener()
			{
				@Override
				public boolean onMyLocationButtonClick()
				{
					if (mLastLocation != null)
					{
						LatLng position = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
						mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
					}
					return true;
				}
			});
		}
	}

	void updateValuesFromBundle(Bundle savedInstanceState)
	{
		// Update the value of mRequestingLocationUpdates from the Bundle, and
		// make sure that the Start Updates and Stop Updates buttons are
		// correctly enabled or disabled.
		if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
		{
			this.mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
		}

		// Update the value of mCurrentLocation from the Bundle and update the
		// UI to show the correct latitude and longitude.
		if (savedInstanceState.keySet().contains(LOCATION_KEY))
		{
			// Since LOCATION_KEY was found in the Bundle, we can be sure that
			// mLastLocation is not null.
			this.mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
			if (this.mLastLocation != null)
			{
				LatLng position = new LatLng(this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());
				this.userLocationMarkerOptions = new MarkerOptions().position(position).title(this.context.getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
			}
		}

		if (savedInstanceState.keySet().contains(FIRST_REQUEST_KEY))
		{
			this.isFirstRequest = savedInstanceState.getBoolean(FIRST_REQUEST_KEY);
		}
	}

	void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, this.mRequestingLocationUpdates);
		savedInstanceState.putBoolean(FIRST_REQUEST_KEY, this.isFirstRequest);
		savedInstanceState.putParcelable(LOCATION_KEY, this.mLastLocation);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		MainActivity act = (MainActivity) this.context;
		if(Utility.isOnline(this.context))
		{
			this.mLastLocation = location;
			updateMap();
			act.updateNoNetworkLabel(false);
		}
		else {
			act.updateNoNetworkLabel(true);
		}

	}

	void getLocation()
	{
		if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
			if (this.mLastLocation != null)
			{
				updateMap();
			}
			if (!this.mRequestingLocationUpdates)
			{
				startLocationUpdates();
				this.mRequestingLocationUpdates = true;
			}
		}
	}

	private void updateMap()
	{
		if (this.mLastLocation != null)
		{
			LatLng userLocation = new LatLng(this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());
			storeLastLocation(userLocation);
			if (this.mMap != null)
			{
				if (this.isFirstRequest)
				{
					this.userLocationMarkerOptions = new MarkerOptions().position(userLocation).title(this.context.getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
					this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
					this.isFirstRequest = false;
				}
				if (this.userMarker == null)
				{
					this.userMarker = this.mMap.addMarker(this.userLocationMarkerOptions);
				}
				this.userMarker.setPosition(userLocation);
			}
		}
	}

	private void restoreLastLocation(){
		SharedPreferences sharedPref = this.context.getPreferences(Context.MODE_PRIVATE);
		float latitude = sharedPref.getFloat("latitude", -1);
		float longitude = sharedPref.getFloat("longitude", -1);
		if(latitude != -1 && longitude != -1){
			mLastLocation = new Location("");
			mLastLocation.setLatitude(latitude);
			mLastLocation.setLongitude(longitude);
			updateMap();
		}

	}

	private void storeLastLocation(LatLng loc){
		SharedPreferences sharedPref = this.context.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putFloat("latitude", (float) loc.latitude);
		editor.putFloat("longitude", (float) loc.longitude);
		editor.commit();
	}

	void checkLocationEnabled()
	{
		if (Utility.isOnline(this.context))
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
							// All location settings are satisfied. The client can initialize location
							// requests here.
							getLocation();
							break;

						case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
							// Location settings are not satisfied. But could be fixed by showing the user
							// a dialog.
							try
							{
								// Show the dialog by calling startResolutionForResult(),
								// and check the result in onActivityResult().
								status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS);
							}
							catch (IntentSender.SendIntentException e)
							{
								// Ignore the error.
							}
							break;

						case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
							// Location settings are not satisfied. However, we have no way to fix the
							// settings so we won't show the dialog.

							break;
					}
				}
			});
		}
		else {
			MainActivity act = (MainActivity) this.context;
			act.updateNoNetworkLabel(true);
			restoreLastLocation();
		}
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
		if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, createLocationRequest(), this);
		}
	}

	void stopLocationUpdates()
	{
		LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
	}
}
