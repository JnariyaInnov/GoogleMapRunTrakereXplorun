package brice.explorun.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import brice.explorun.models.Utility;
import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.models.Observer;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.services.LocationService;
import brice.explorun.R;

public class MapFragment extends PlacesObserverFragment implements Observer, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
	private final String LOCATION_KEY = "location";
	private final String FIRST_REQUEST_KEY = "first_request";
	private final String PLACES_KEY = "places";
	private final int MY_PERMISSIONS_REQUEST_GPS = 0;

	private Bundle args = null;
	private GoogleMap map = null;
	private MarkerOptions userLocationMarkerOptions;
	private Marker userMarker;
	private boolean isFirstRequest = true;

	private GoogleApiClient mGoogleApiClient = null;
	private LocationService locationService;
	private NearbyAttractionsController nearbyAttractionsController;

	private ArrayList<Place> places;
	private ArrayList<Marker> placesMarkers;

	private List<String> types;

	public LocationService getLocationService()
	{
		return this.locationService;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);
		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null)
		{
			this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.addApi(Places.GEO_DATA_API)
					.build();
		}

		this.args = this.getArguments();

		this.locationService = new LocationService(this, this.mGoogleApiClient);

		this.places = new ArrayList<>();
		this.placesMarkers = new ArrayList<>();

		this.types = Arrays.asList(getResources().getStringArray(R.array.places_types));

		this.nearbyAttractionsController = new NearbyAttractionsController(this, this.mGoogleApiClient);

		return view;
	}

	private void updateValuesFromBundle(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			// Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
			if (savedInstanceState.keySet().contains(LOCATION_KEY))
			{
				// Since LOCATION_KEY was found in the Bundle, we can be sure that LastLocation is not null.
				this.locationService.setLastLocation((Location) savedInstanceState.getParcelable(LOCATION_KEY));
				Location loc = this.locationService.getLastLocation();
				if (loc != null)
				{
					LatLng position = new LatLng(loc.getLatitude(), loc.getLongitude());
					this.userLocationMarkerOptions = new MarkerOptions().position(position).title(getContext().getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				}
			}

			if (savedInstanceState.keySet().contains(FIRST_REQUEST_KEY))
			{
				this.isFirstRequest = savedInstanceState.getBoolean(FIRST_REQUEST_KEY);
			}

			if (savedInstanceState.keySet().contains(PLACES_KEY))
			{
				this.places = savedInstanceState.getParcelableArrayList(PLACES_KEY);
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
		if (!this.mGoogleApiClient.isConnected())
		{
			this.mGoogleApiClient.connect();
		}
	}

	public void onStop()
	{
		if (this.mGoogleApiClient.isConnected())
		{
			this.locationService.stopLocationUpdates();
			this.mGoogleApiClient.disconnect();
		}
		super.onStop();
	}

	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		this.locationService.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(FIRST_REQUEST_KEY, this.isFirstRequest);
		savedInstanceState.putParcelableArrayList(PLACES_KEY, this.places);
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
			this.locationService.checkLocationEnabled();
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
					this.getLocationService().checkLocationEnabled();
					if (this.map != null)
					{
						initializeMyLocationButton();
					}
				}
			}
			break;
		}
	}

	@Override
	public void onMapReady(final GoogleMap googleMap)
	{
		//Initialize map
		UiSettings settings = googleMap.getUiSettings();
		settings.setZoomControlsEnabled(true);
		this.map = googleMap;
		initializeMyLocationButton();
		// Retrieve places markers if orientation has changed
		addPlacesMarkers();
		// Move the camera over a place if the user has clicked on one attraction in the list in the NearbyAttractionsFragment
		if (this.isFirstRequest && this.args != null)
		{
			LatLng placeLocation = new LatLng(this.args.getDouble("latitude"), this.args.getDouble("longitude"));
			this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 15));
		}
	}

	public void initializeMyLocationButton()
	{
		if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			this.map.setMyLocationEnabled(true);
		}
	}

	@Override
	public void update()
	{
		if (this.locationService.getLastLocation() != null)
		{
			LatLng userLocation = new LatLng(this.locationService.getLastLocation().getLatitude(), this.locationService.getLastLocation().getLongitude());
			this.locationService.storeLastLocation(userLocation);
			if (this.map != null)
			{
				if (this.isFirstRequest && this.args == null)
				{
					this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
				}
				if (this.userMarker == null)
				{
					this.userLocationMarkerOptions = new MarkerOptions().position(userLocation).title(getContext().getResources().getString(R.string.your_position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
					this.userMarker = this.map.addMarker(this.userLocationMarkerOptions);
				}
				this.userMarker.setPosition(userLocation);
				this.isFirstRequest = false;
			}
			getNearbyPlaces();
		}
	}

	public void restoreLastLocation()
	{
		this.locationService.updateLocationFromPreferences();
		update();
	}

	public void getNearbyPlaces()
	{
		this.nearbyAttractionsController.getNearbyPlaces();
	}

	@Override
	public void updatePlaces(ArrayList<Place> places)
	{
		this.removeMarkers();
		this.places.clear();
		this.places.addAll(places);
		addPlacesMarkers();
	}

	public void addPlacesMarkers()
	{
		for (Place place: this.places)
		{
			LatLng location = new LatLng(place.getLatitude(), place.getLongitude());
			MarkerOptions options = new MarkerOptions().title(place.getName()).position(location);
			options.icon(BitmapDescriptorFactory.defaultMarker(getPlaceMarkerColor(place)));
			Marker marker = this.map.addMarker(options);
			this.placesMarkers.add(marker);

			// Open info window of the place marker if the user has clicked one attraction from NearbyAttractionsFragment
			if (this.args != null)
			{
				LatLng placeLocation = new LatLng(this.args.getDouble("latitude"), this.args.getDouble("longitude"));
				if (location.equals(placeLocation))
				{
					marker.showInfoWindow();
				}
			}
		}
	}

	public void removeMarkers()
	{
		for (Marker m: this.placesMarkers)
		{
			m.remove();
		}
		this.placesMarkers.clear();
	}

	@Override
	public void updatePlacePhoto(Photo photo)
	{

	}

	public float getPlaceMarkerColor(Place place)
	{
		ArrayList<String> types = place.getTypes();
		String res = "";
		int i = 0;
		boolean found = false;
		while (!found && i < types.size())
		{
			String type = types.get(i);
			if (this.types.contains(type))
			{
				found = true;
				res = type;
			}
			else
			{
				i++;
			}
		}

		return Utility.getColorFromType(this.getActivity(), res);
	}
}
