package brice.explorun;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
	private final String LOCATION_KEY = "location";
	private final int MY_PERMISSIONS_REQUEST_GPS = 0;
	private final int checkInterval = 5000;
	private GoogleMap map = null;
	private MarkerOptions userLocationMarkerOptions;
	private Marker userMarker;

	private GoogleApiClient mGoogleApiClient = null;
	private LocationManager locationManager;
	public LocationManager getLocationManager()
	{
		return this.locationManager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_maps, container, false);
		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null) {
			this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}

		this.locationManager = new LocationManager(this, this.mGoogleApiClient);
		new NetworkHandler(this.getActivity(), (TextView) view.findViewById(R.id.no_network_label), checkInterval);

		return view;
	}

	private void updateValuesFromBundle(Bundle savedInstanceState)
	{
		if (savedInstanceState != null) {
			// Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
			if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
				// Since LOCATION_KEY was found in the Bundle, we can be sure that LastLocation is not null.
				this.locationManager.setLastLocation((Location) savedInstanceState.getParcelable(LOCATION_KEY));
				Location loc = locationManager.getmLastLocation();
				if (loc != null) {
					LatLng position = new LatLng(loc.getLatitude(), loc.getLongitude());
					this.userLocationMarkerOptions = new MarkerOptions().position(position).title(getContext().getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				}
			}
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		updateValuesFromBundle(savedInstanceState);
		SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	public void onStart()
	{
		super.onStart();
		this.mGoogleApiClient.connect();
	}

	public void onStop()
	{
		this.locationManager.stopLocationUpdates();
		this.mGoogleApiClient.disconnect();
		super.onStop();
	}

	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		this.locationManager.onSaveInstanceState(savedInstanceState);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onConnected(Bundle connectionHint)
	{
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
		}
		else
		{
			this.locationManager.checkLocationEnabled();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case MY_PERMISSIONS_REQUEST_GPS:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					this.getLocationManager().checkLocationEnabled();
				}
			}
			break;
		}
	}

	@Override
	public void onMapReady(final GoogleMap googleMap)
	{
		//Initialize map
		final LocationManager lm = this.locationManager;
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			googleMap.setMyLocationEnabled(true);
			googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

				@Override
				public boolean onMyLocationButtonClick() {
					if (lm != null) {
						LatLng position = new LatLng(lm.getmLastLocation().getLatitude(), lm.getmLastLocation().getLongitude());
						googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
					}
					return true;
				}
			});
		}
		UiSettings settings = googleMap.getUiSettings();
		settings.setZoomControlsEnabled(true);
		this.map = googleMap;
	}

	public void updateMap()
	{
		if (this.locationManager.getmLastLocation() != null)
		{
			LatLng userLocation = new LatLng(this.locationManager.getmLastLocation().getLatitude(), this.locationManager.getmLastLocation().getLongitude());
			this.locationManager.storeLastLocation(userLocation);
			if (this.map != null) {
				this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
				if (this.userMarker == null) {
					this.userLocationMarkerOptions = new MarkerOptions().position(userLocation).title(getContext().getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
					this.userMarker = this.map.addMarker(this.userLocationMarkerOptions);
				}
				this.userMarker.setPosition(userLocation);
			}
		}
	}

	public void restoreLastLocation()
	{
		this.locationManager.getLocationFromPreferences();
		updateMap();
	}
}
