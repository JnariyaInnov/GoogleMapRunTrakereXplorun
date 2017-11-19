package brice.explorun.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.akexorcist.googledirection.model.Coordination;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import brice.explorun.activities.MainActivity;
import brice.explorun.controllers.RoutesController;
import brice.explorun.models.FormObserver;
import brice.explorun.models.Utility;
import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.R;
import brice.explorun.services.LocationService;

public class MapFragment extends PlacesObserverFragment implements OnMapReadyCallback, FormObserver
{
	private final String LOCATION_KEY = "location";
	private final String FIRST_REQUEST_KEY = "first_request";
	private final String PLACES_KEY = "places";
	private final String FORM_OPEN_KEY = "form_open";
	private final String ROUTE_KEY = "route";

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

	private RelativeLayout layout;
	private Button mFormButton;
	private ScrollView formLayout;
	private Animation slideUpAnimation;
	private Animation slideDownAnimation;
	private ProgressBar progressBar = null;

	private RoutesController routesController;
	private ArrayList<Polyline> polylines;
	private Route route;

	private int width;
	private int height;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		this.args = this.getArguments();

		this.places = new ArrayList<>();
		this.placesMarkers = new ArrayList<>();

		this.types = Arrays.asList(this.getResources().getStringArray(R.array.places_types));

		this.nearbyAttractionsController = new NearbyAttractionsController(this);
		IntentFilter filter = new IntentFilter("ex_location");
		getActivity().registerReceiver(locReceiver, filter);

		this.layout = view.findViewById(R.id.map_fragment_view);
		this.formLayout = view.findViewById(R.id.form);
		this.formLayout.setVisibility(View.GONE);
		this.slideUpAnimation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_up);
		this.slideDownAnimation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_down);

		mFormButton = view.findViewById(R.id.form_btn);
		mFormButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				formLayout.setAnimation(slideUpAnimation);
				formLayout.setVisibility(View.VISIBLE);
				formLayout.startAnimation(slideUpAnimation);
			}
		});
		this.progressBar = view.findViewById(R.id.progress_bar);

		float[] userLoc = Utility.getLocationFromPreferences(getActivity());
		this.routesController = new RoutesController(this, places, userLoc);
		this.polylines = new ArrayList<>();
		this.route = new Route();

		this.width = getResources().getDisplayMetrics().widthPixels;
		this.height = getResources().getDisplayMetrics().heightPixels;

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
			if (savedInstanceState.keySet().contains(PLACES_KEY))
			{
				this.places = savedInstanceState.getParcelableArrayList(PLACES_KEY);
				this.routesController.setPlaces(this.places);
			}

			// Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
			if (savedInstanceState.keySet().contains(LOCATION_KEY) && this.places.size() > 0)
			{
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

			if (savedInstanceState.keySet().contains(FORM_OPEN_KEY))
			{
				this.formLayout.setVisibility(savedInstanceState.getInt(FORM_OPEN_KEY));
			}

			if (savedInstanceState.keySet().contains(ROUTE_KEY))
			{
				this.route = savedInstanceState.getParcelable(ROUTE_KEY);
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
		savedInstanceState.putParcelable(ROUTE_KEY, this.route);
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
		// Retrieve places markers and route if orientation has changed
		addPlacesMarkers();
		drawRouteOnMap(this.route);
		// Move the camera if the user has clicked on one attraction in the list in the NearbyAttractionsFragment
		if (this.isFirstRequest && this.args != null)
		{
			LatLng loc1 = new LatLng(this.args.getDouble("latitude"), this.args.getDouble("longitude"));

			float[] loc = Utility.getLocationFromPreferences(this.getActivity());
			LatLng loc2 = new LatLng(loc[0], loc[1]);

			List<LatLng> locations = Arrays.asList(loc1, loc2);

			int padding = getPaddingForCameraBetweenTwoLocations(loc1, loc2);

			map.moveCamera(Utility.getCameraUpdateBounds(this.width, this.height, padding, locations));
		}
		update();
	}

	public int getPaddingForCameraBetweenTwoLocations(LatLng loc1, LatLng loc2)
	{
		double deltaLatitude = Math.abs(loc1.latitude - loc2.latitude);
		double deltaLongitude = Math.abs(loc1.longitude - loc2.longitude);

		double delta = deltaLongitude-deltaLatitude;

		int padding;

		Configuration config = getResources().getConfiguration();
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			padding = (int) (this.height * 0.2); // offset from edges of the map 20% of the height of the screen
			if (delta > 0)
			{
				padding = (int) (this.width * 0.1); // offset from edges of the map 10% of the width of the screen
			}
		}
		else // If we are in landscape mode
		{
			padding = (int) (this.width * 0.15); // offset from edges of the map 15% of the width of the screen
			if (delta > 0)
			{
				padding = (int) (this.height * 0.25); // offset from edges of the map 25% of the height of the screen
			}
		}

		return padding;
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
		this.routesController.setUserLocation(loc);
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
				getNearbyPlaces();
			}
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
			this.routesController.setPlaces(places);
		}
		this.places.addAll(places);
		addPlacesMarkers();
	}

	public void addPlacesMarkers()
	{
		for (Place place: this.places)
		{
			LatLng location = new LatLng(place.getLatitude(), place.getLongitude());
			if (this.map != null)
			{
				MarkerOptions options = new MarkerOptions().title(place.getName()).position(location);
				options.icon(BitmapDescriptorFactory.defaultMarker(Utility.getPlaceMarkerColor(this, place)));
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

	public void removePolylines()
	{
		for (Polyline p: this.polylines)
		{
			p.remove();
		}
		this.polylines.clear();
	}

	@Override
	public void updatePlacePhoto(Photo photo)
	{
		//todo
	}

	@Override
	public void onFormValidate(int sport, int leftPinValue, int rightPinValue)
	{
		this.showProgressBar();

		int averageSpeed = Utility.getAverageSpeedFromSport(sport);
		double minKM = leftPinValue / 60.0 * averageSpeed;
		double maxKM = rightPinValue / 60.0 * averageSpeed;

		ArrayList<Place> route = this.routesController.generateRoute(minKM, maxKM);
		if (route != null)
		{
			if (Utility.isOnline(this.getActivity()))
			{
				this.routesController.getRouteFromAPI(route);
			}
			else
			{
				this.hideProgressBar();
				Toast.makeText(this.getActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			this.hideProgressBar();
			Toast.makeText(this.getActivity(), R.string.no_route_found, Toast.LENGTH_LONG).show();
		}
	}

	public void drawRouteOnMap(Route route)
	{
		if (this.map != null)
		{
			this.route = route;
			this.removePolylines();

			List<Leg> legList = route.getLegList();

			int blue = ContextCompat.getColor(this.getActivity(), R.color.lightBlue);

			if (legList != null)
			{
				int legNumber = 0;
				for (Leg leg : legList)
				{
					ArrayList<LatLng> directionPoints = leg.getDirectionPoint();

					PolylineOptions polylineOptions = DirectionConverter.createPolyline(this.getActivity(), directionPoints, 5, Color.BLACK);
					polylineOptions.zIndex(-1);
					this.polylines.add(this.map.addPolyline(polylineOptions));

					PolylineOptions polylineOptions2 = DirectionConverter.createPolyline(this.getActivity(), directionPoints, 2, blue);
					polylineOptions2.zIndex(legNumber);
					this.polylines.add(this.map.addPolyline(polylineOptions2));

					legNumber++;
				}
			}
		}
	}

	public void onDirectionAPIResponse(Route route)
	{
		this.hideProgressBar();

		if (route != null)
		{
			// Close form fragment
			this.formLayout.setAnimation(this.slideDownAnimation);
			this.formLayout.setVisibility(View.GONE);
			this.formLayout.startAnimation(this.slideDownAnimation);

			this.drawRouteOnMap(route);

			List<LatLng> locations = new ArrayList<>();
			List<Step> instructions = new ArrayList<>();
			String currentInstruction;

			for (Leg leg: route.getLegList())
			{
				Coordination coordination = leg.getStartLocation();
				locations.add(new LatLng(coordination.getLatitude(), coordination.getLongitude()));
				for(Step step : leg.getStepList()){
					step.setHtmlInstruction(step.getHtmlInstruction().replace("<div", ".<div").replaceAll("\\<.*?>",""));
					instructions.add(step);
					Log.d("eX_instruction", step.getHtmlInstruction());
				}
			}

			LocationService.setInstructions(instructions);

			this.map.animateCamera(Utility.getCameraUpdateBounds(this.width, this.height, (int) (this.height*0.15), locations));
		}
		else
		{
			Toast.makeText(this.getActivity(), R.string.directions_api_request_error, Toast.LENGTH_LONG).show();
		}
	}

	public void showProgressBar()
	{
		this.layout.setAlpha(0.6f);
		this.progressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar()
	{
		this.progressBar.setVisibility(View.GONE);
		this.layout.setAlpha(1);
	}
}
