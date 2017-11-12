package brice.explorun.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import brice.explorun.activities.MainActivity;
import brice.explorun.controllers.ParcoursController;
import brice.explorun.models.Utility;
import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.services.LocationService;
import brice.explorun.R;

public class MapFragment extends PlacesObserverFragment implements OnMapReadyCallback
{
	private final String LOCATION_KEY = "location";
	private final String FIRST_REQUEST_KEY = "first_request";
	private final String PLACES_KEY = "places";
	private final String FORM_OPEN_KEY = "form_open";

	private Bundle args = null;
	private GoogleMap map = null;
	private MarkerOptions userLocationMarkerOptions;
	private Marker userMarker;
	private boolean isFirstRequest = true;

	private BroadcastReceiver locReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			update();
		}
	};

	private ArrayList<Place> places;
	private ArrayList<Marker> placesMarkers;

	private List<String> types;

	private Button mFormButton;
	private ScrollView formLayout;
	private Animation animation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		this.args = this.getArguments();

		this.places = new ArrayList<>();
		this.placesMarkers = new ArrayList<>();

		this.types = Arrays.asList(getResources().getStringArray(R.array.places_types));

		this.nearbyAttractionsController = new NearbyAttractionsController(this);
		IntentFilter filter = new IntentFilter("ex_location");
		getActivity().registerReceiver(locReceiver, filter);

		this.formLayout = view.findViewById(R.id.form);
		this.animation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_up);
		this.formLayout.setAnimation(this.animation);

		mFormButton = view.findViewById(R.id.form_btn);
		mFormButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				formLayout.setVisibility(View.VISIBLE);
				formLayout.startAnimation(animation);
			}
		});

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(this.locReceiver);
	}

	private void updateValuesFromBundle(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			// Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
			if (savedInstanceState.keySet().contains(LOCATION_KEY))
			{
				savedInstanceState.getParcelable(LOCATION_KEY);
				// Since LOCATION_KEY was found in the Bundle, we can be sure that LastLocation is not null.
				mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
				if (mLastLocation != null)
				{
					LatLng position = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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

			if (savedInstanceState.keySet().contains(FORM_OPEN_KEY))
			{
				this.formLayout.setVisibility(savedInstanceState.getInt(FORM_OPEN_KEY));
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
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getConnectivityStatusHandler().updateNoNetworkLabel();
	}

	public void onStart()
	{
		super.onStart();
	}

	public void onStop()
	{
		super.onStop();
	}

	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putBoolean(FIRST_REQUEST_KEY, this.isFirstRequest);
		savedInstanceState.putInt(FORM_OPEN_KEY, this.formLayout.getVisibility());
		savedInstanceState.putParcelableArrayList(PLACES_KEY, this.places);
		savedInstanceState.putParcelable(LOCATION_KEY, this.mLastLocation);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onMapReady(final GoogleMap googleMap)
	{
		//Initialize map
		UiSettings settings = googleMap.getUiSettings();
		settings.setZoomControlsEnabled(true);
		this.map = googleMap;
		if(this.places.size() == 0 && this.args != null){
			this.places = args.getParcelableArrayList("places");
		}
		// Retrieve places markers if orientation has changed
		addPlacesMarkers();
		// Move the camera if the user has clicked on one attraction in the list in the NearbyAttractionsFragment
		if (this.isFirstRequest && this.args != null)
		{
			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			double latitude = this.args.getDouble("latitude");
			double longitude = this.args.getDouble("longitude");
			// Include place's position
			builder.include(new LatLng(latitude, longitude));
			// Include user's position
			float[] loc = Utility.getLocationFromPreferences(this.getActivity());
			builder.include(new LatLng(loc[0], loc[1]));

			double deltaLatitude = Math.abs(latitude - loc[0]);
			double deltaLongitude = Math.abs(longitude - loc[1]);

			double delta = deltaLongitude-deltaLatitude;

			int width = getResources().getDisplayMetrics().widthPixels;
			int height = getResources().getDisplayMetrics().heightPixels;
			int padding;

			Configuration config = getResources().getConfiguration();
			if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				padding = (int) (height * 0.2); // offset from edges of the map 20% of the height of the screen
				if (delta > 0)
				{
					padding = (int) (width * 0.1); // offset from edges of the map 10% of the width of the screen
				}
			}
			else // If we are in landscape mode
			{
				padding = (int) (width * 0.15); // offset from edges of the map 15% of the width of the screen
				if (delta > 0)
				{
					padding = (int) (height * 0.25); // offset from edges of the map 25% of the height of the screen
				}
			}

			LatLngBounds bounds = builder.build();

			map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
		}
		update();
	}

	public void initializeMyLocationButton()
	{
		if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			this.map.setMyLocationEnabled(true);
		}
	}

	public void update()
	{
		float[] loc = Utility.getLocationFromPreferences(this.getActivity());
		float latitude = loc[0];
		float longitude = loc[1];
		if (latitude != -1f && longitude != -1f)
		{
			LatLng userLocation = new LatLng(latitude, longitude);
			if (this.map != null)
			{
				if (!this.map.isMyLocationEnabled())
				{
					initializeMyLocationButton();
				}
				if (this.isFirstRequest && this.args == null)
				{
					this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
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

	@Override
	public void updatePlaces(ArrayList<Place> places, int errorsCount)
	{
		super.updatePlaces(places, errorsCount);
		if (errorsCount < this.types.size())
		{
			this.removeMarkers();
			this.places.clear();
		}
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
			if (this.map != null)
			{
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
		//todo
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

		return Utility.getColorFromType(this, res);
	}

	public ArrayList<Place> generateParcours(){
		float[] userLoc = Utility.getLocationFromPreferences(getActivity());
		ParcoursController pc = new ParcoursController(places, userLoc);
		ArrayList<Place> parcours = pc.generateParcours(5, 5.5);
		pc.printParcours(parcours);
		return parcours;
	}
}
