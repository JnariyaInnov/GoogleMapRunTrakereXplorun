package brice.explorun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener
{
	private GoogleMap mMap = null;
	private final int MY_PERMISSIONS_REQUEST_GPS = 0;
	private GoogleApiClient mGoogleApiClient = null;

	private boolean firstRequest = true;
	private boolean mRequestingLocationUpdates = false;
	private Location mLastLocation = null;
	private MarkerOptions userLocationMarkerOptions;
	private Marker userMarker;

	private final String REQUESTING_LOCATION_UPDATES_KEY = "requesting";
	private final String LOCATION_KEY = "location";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_maps, container, false);
		super.onCreate(savedInstanceState);

		MapsInitializer.initialize(this.getActivity());

		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null) {
			this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}

		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		this.userLocationMarkerOptions = new MarkerOptions().position(new LatLng(0,0)).title(this.getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

		updateValuesFromBundle(savedInstanceState);

		return view;
	}

	private void updateValuesFromBundle(Bundle savedInstanceState) {
		if (savedInstanceState != null)
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
					this.userLocationMarkerOptions.position(position);
				}
			}
		}
	}

	public void onStart() {
		super.onStart();
		this.mGoogleApiClient.connect();
	}

	public void onStop() {
		this.stopLocationUpdates();
		this.mGoogleApiClient.disconnect();
		super.onStop();
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, this.mRequestingLocationUpdates);
		savedInstanceState.putParcelable(LOCATION_KEY, this.mLastLocation);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
		}
		else
		{
			getLocation();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {
		this.mLastLocation = location;
		Log.d("onLocationChanged", location.toString());
		updateMap();
	}


	public void getLocation() {
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.mMap = googleMap;
		UiSettings settings = this.mMap.getUiSettings();
		settings.setZoomControlsEnabled(true);
		this.userMarker = this.mMap.addMarker(this.userLocationMarkerOptions);
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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

	public void updateMap() {
		if (this.mLastLocation != null)
		{
			LatLng userLocation = new LatLng(this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());
			if (this.mMap != null)
			{
				if (this.firstRequest)
				{
					this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
					this.firstRequest = false;
				}
				this.userMarker.setPosition(userLocation);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode)
		{
			case MY_PERMISSIONS_REQUEST_GPS:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					getLocation();
				}
			}
			break;
		}
	}

	protected LocationRequest createLocationRequest() {
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(10000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		return mLocationRequest;
	}

	protected void startLocationUpdates() {
		if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, createLocationRequest(), this);
		}
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
	}
}
