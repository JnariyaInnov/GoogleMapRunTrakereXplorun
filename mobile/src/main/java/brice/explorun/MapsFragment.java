package brice.explorun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
	private final int MY_PERMISSIONS_REQUEST_GPS = 0;
	private GoogleApiClient mGoogleApiClient = null;

	private TextView noNetworkLabel;

	private LocationManager locationManager;
	public LocationManager getLocationManager()
	{
		return this.locationManager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_maps, container, false);

		this.noNetworkLabel = view.findViewById(R.id.no_network_label);

		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null)
		{
			this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}

		this.locationManager = new LocationManager(this.getActivity(), this.mGoogleApiClient);

		if (!Utility.isOnline(this.getActivity()))
		{
			updateNoNetworkLabel(true);
			final Handler ha = new Handler();
			final MainActivity mainActivity = (MainActivity) getActivity();
			ha.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					//call function
					if(Utility.isOnline(mainActivity.getApplicationContext()))
					{
						updateNoNetworkLabel(false);
					}
					else
					{
						ha.postDelayed(this, 10000);
					}
				}
			}, 10000);
		}

		return view;
	}

	private void updateValuesFromBundle(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			this.locationManager.updateValuesFromBundle(savedInstanceState);
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
	public void onMapReady(GoogleMap googleMap)
	{
		this.locationManager.setMap(googleMap);
		UiSettings settings = googleMap.getUiSettings();
		settings.setZoomControlsEnabled(true);
	}

	public void updateNoNetworkLabel(boolean visible)
	{
		if (visible)
		{
			this.noNetworkLabel.setVisibility(View.VISIBLE);
		}
		else
		{
			this.noNetworkLabel.setVisibility(View.GONE);
		}
	}
}
