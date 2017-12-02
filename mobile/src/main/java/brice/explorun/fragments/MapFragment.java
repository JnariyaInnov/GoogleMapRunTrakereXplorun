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
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import java.util.List;

import brice.explorun.activities.MainActivity;
import brice.explorun.controllers.RoutesController;
import brice.explorun.models.CustomRoute;
import brice.explorun.models.FirebasePlace;
import brice.explorun.models.FirebaseRoute;
import brice.explorun.models.Position;
import brice.explorun.models.RouteObserver;
import brice.explorun.services.RouteService;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.Utility;
import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.R;

public class MapFragment extends PlacesObserverFragment implements OnMapReadyCallback, RouteObserver
{
	private final String LOCATION_KEY = "location";
	private final String FIRST_REQUEST_KEY = "first_request";
	private final String PLACES_KEY = "places";
	private final String FORM_OPEN_KEY = "form_open";
	private final String ROUTE_INFO_OPEN_KEY = "route_info_open";
	private final String CURRENT_ROUTE_OPEN_KEY = "current_route_open";
	private final String ROUTE_KEY = "route";
	private final String CUSTOM_ROUTE_KEY = "custom_route";

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
	private GoogleMap.OnPolylineClickListener polylineClickListener;
	private GoogleMap.OnMapClickListener mapClickListener;
	private View.OnClickListener searchRouteClickListener;
	private View.OnClickListener infoRouteClickListener;

	private ArrayList<Place> places;
	private ArrayList<Marker> placesMarkers;

	private List<String> types;

	private RelativeLayout layout;
	private Button mFormButton;
	private ScrollView routeCreationLayout;
	private ScrollView routeInfoLayout;
	private ScrollView currentRouteLayout;
	private Animation slideUpAnimation;
	private Animation slideDownAnimation;
	private ProgressBar progressBar = null;

	private RoutesController routesController;
	private ArrayList<Polyline> polylines;
	private CustomRoute customRoute;
	private Route route;

	private int width;
	private int height;
	private int routeInfoLayoutVisibility = View.GONE;

	private RouteInfoFragment routeInfoFragment;
	private CurrentRouteFragment currentRouteFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		this.args = this.getArguments();

		this.polylineClickListener = new GoogleMap.OnPolylineClickListener()
		{
			@Override
			public void onPolylineClick(Polyline polyline)
			{
				if (routeInfoLayout.getVisibility() == View.GONE)
				{
					showRouteInfo();
				}
			}
		};
		this.mapClickListener = new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng latLng)
			{
				slideDownFragments();
			}
		};
		this.searchRouteClickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				slideUpFragment(routeCreationLayout);
			}
		};
		this.infoRouteClickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				slideUpFragment(routeInfoLayout);
			}
		};

		this.places = new ArrayList<>();
		this.placesMarkers = new ArrayList<>();

		this.types = Arrays.asList(this.getResources().getStringArray(R.array.places_types));

		this.nearbyAttractionsController = new NearbyAttractionsController(this);
		IntentFilter filter = new IntentFilter("ex_location");
		getActivity().registerReceiver(locReceiver, filter);

		this.layout = view.findViewById(R.id.map_fragment_view);
		this.routeCreationLayout = view.findViewById(R.id.form);
		this.routeInfoLayout = view.findViewById(R.id.route_info);
		this.routeInfoLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				routeInfoLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				routeInfoLayout.setVisibility(routeInfoLayoutVisibility);
			}
		});
		this.currentRouteLayout = view.findViewById(R.id.current_route);
		this.slideUpAnimation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_up);
		this.slideDownAnimation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_down);

		this.mFormButton = view.findViewById(R.id.form_btn);
		this.mFormButton.setOnClickListener(this.searchRouteClickListener);
		this.progressBar = view.findViewById(R.id.progress_bar);

		float[] userLoc = LocationUtility.getLocationFromPreferences(getActivity());
		this.routesController = new RoutesController(this, places, userLoc);
		this.polylines = new ArrayList<>();
		this.route = new Route();

		this.width = getResources().getDisplayMetrics().widthPixels;
		this.height = getResources().getDisplayMetrics().heightPixels;

		this.routeInfoFragment = (RouteInfoFragment) this.getChildFragmentManager().findFragmentById(R.id.route_info);
		this.currentRouteFragment = (CurrentRouteFragment) this.getChildFragmentManager().findFragmentById(R.id.current_route);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try
		{
			getActivity().unregisterReceiver(this.locReceiver);
		}
		catch (IllegalArgumentException e)
		{
			Log.d("MapFragment", "Receiver not registered");
		}
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
				this.routeCreationLayout.setVisibility(savedInstanceState.getInt(FORM_OPEN_KEY));
			}

			if (savedInstanceState.keySet().contains(ROUTE_INFO_OPEN_KEY))
			{
				this.routeInfoLayoutVisibility = savedInstanceState.getInt(ROUTE_INFO_OPEN_KEY);
			}

			if (savedInstanceState.keySet().contains(CURRENT_ROUTE_OPEN_KEY))
			{
				this.currentRouteLayout.setVisibility(savedInstanceState.getInt(CURRENT_ROUTE_OPEN_KEY));
			}

			if (savedInstanceState.keySet().contains(ROUTE_KEY))
			{
				this.route = savedInstanceState.getParcelable(ROUTE_KEY);
			}

			if (savedInstanceState.keySet().contains(CUSTOM_ROUTE_KEY))
			{
				this.customRoute = savedInstanceState.getParcelable(CUSTOM_ROUTE_KEY);
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
		savedInstanceState.putInt(FORM_OPEN_KEY, this.routeCreationLayout.getVisibility());
		savedInstanceState.putInt(ROUTE_INFO_OPEN_KEY, this.routeInfoLayout.getVisibility());
		savedInstanceState.putInt(CURRENT_ROUTE_OPEN_KEY, this.currentRouteLayout.getVisibility());
		savedInstanceState.putParcelableArrayList(PLACES_KEY, this.places);
		savedInstanceState.putParcelable(LOCATION_KEY, this.mLastLocation);
		savedInstanceState.putParcelable(ROUTE_KEY, this.route);
		savedInstanceState.putParcelable(CUSTOM_ROUTE_KEY, this.customRoute);
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
			this.routesController.setPlaces(this.places);
		}
		// Retrieve places markers and route if orientation has changed
		addPlacesMarkers();
		drawRouteOnMap(this.route);
		// Move the camera if the user has clicked on one attraction in the list in the NearbyAttractionsFragment
		if (this.isFirstRequest && this.args != null && this.args.containsKey("latitude"))
		{
			LatLng loc1 = new LatLng(this.args.getDouble("latitude"), this.args.getDouble("longitude"));

			float[] loc = LocationUtility.getLocationFromPreferences(this.getActivity());
			LatLng loc2 = new LatLng(loc[0], loc[1]);

			List<LatLng> locations = Arrays.asList(loc1, loc2);

			int padding = getPaddingForCameraBetweenTwoLocations(loc1, loc2);

			map.moveCamera(Utility.getCameraUpdateBounds(this.width, this.height, padding, locations));
		}
		if (this.isFirstRequest && this.args != null && this.args.containsKey("route"))
		{
			FirebaseRoute route = this.args.getParcelable("route");
			if (route != null)
			{
				ArrayList<Place> places = new ArrayList<>();
				for (FirebasePlace p : route.getPlaces())
				{
					places.add(new Place(p.getName(), p.getPosition()));
				}
				this.customRoute = new CustomRoute(route.getStartPosition(), route.getSportType(), places);
				this.customRoute.setDuration(route.getDuration());
				this.customRoute.setDistance(route.getDistance());
				this.showProgressBar();
				this.getRouteFromAPI();
			}
		}
		this.map.setOnPolylineClickListener(this.polylineClickListener);
		this.map.setOnMapClickListener(this.mapClickListener);
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
		float[] loc = LocationUtility.getLocationFromPreferences(this.getActivity());
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
				if (this.isFirstRequest && (this.args == null || this.args.containsKey("route")))
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
				if (!RouteService.isStarted)
				{
					getNearbyPlaces();
				}
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
			if (this.places != null)
			{
				this.places.clear();
			}
			else
			{
				this.places = new ArrayList<>();
			}
			this.routesController.setPlaces(places);
		}
		this.places.addAll(places);
		addPlacesMarkers();
	}

	public void addPlacesMarkers()
	{
		if (this.places != null)
		{
			for (Place place : this.places)
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

		float averageSpeed = SportUtility.getAverageSpeedFromSport(sport);
		double minKM = leftPinValue / 60.0 * averageSpeed;
		double maxKM = rightPinValue / 60.0 * averageSpeed;

		ArrayList<Place> route = this.routesController.generateRoute(minKM, maxKM);
		float[] pos = LocationUtility.getLocationFromPreferences(this.getActivity());
		this.customRoute = new CustomRoute(new Position(pos[0], pos[1]), sport, route);
		if (route != null)
		{
			this.getRouteFromAPI();
		}
		else
		{
			this.hideProgressBar();
			Toast.makeText(this.getActivity(), R.string.no_route_found, Toast.LENGTH_SHORT).show();
		}
	}

	public void getRouteFromAPI()
	{
		if (Utility.isOnline(this.getActivity()))
		{
			this.routesController.getRouteFromAPI(this.customRoute);
		}
		else
		{
			this.hideProgressBar();
			Toast.makeText(this.getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
		}
	}

	public void drawRouteOnMap(Route route)
	{
		if (this.map != null)
		{
			this.route = route;
			this.removePolylines();

			if (this.route != null)
			{
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
						polylineOptions.clickable(true);
						this.polylines.add(this.map.addPolyline(polylineOptions));

						polylineOptions = DirectionConverter.createPolyline(this.getActivity(), directionPoints, 2, blue);
						polylineOptions.zIndex(legNumber);
						polylineOptions.clickable(true);
						this.polylines.add(this.map.addPolyline(polylineOptions));

						legNumber++;
					}
					this.routeInfoFragment.update(this.customRoute);
					if (this.currentRouteLayout.getVisibility() == View.VISIBLE)
					{
						this.currentRouteFragment.update(this.customRoute);
					}

					this.mFormButton.setText(R.string.route_info_text);
					this.mFormButton.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(R.drawable.ic_info), null, null, null);
					this.mFormButton.setOnClickListener(this.infoRouteClickListener);
				}
			}
		}
	}

	public void showRouteInfo()
	{
		if (this.routeCreationLayout.getVisibility() == View.VISIBLE)
		{
			this.slideDownFragment(this.routeCreationLayout);
		}
		if (this.routeInfoLayout.getVisibility() == View.GONE)
		{
			this.slideUpFragment(this.routeInfoLayout);
		}
	}

	public void onDirectionAPIResponse(Route route)
	{
		this.hideProgressBar();

		if (route != null)
		{
			// Close form fragment
			this.slideDownFragment(this.routeCreationLayout);

			List<LatLng> locations = new ArrayList<>();
			ArrayList<Step> instructions = new ArrayList<>();

			float distance = 0;

			for (Leg leg: route.getLegList())
			{
				distance += Float.parseFloat(leg.getDistance().getValue());
				Coordination coordination = leg.getStartLocation();
				locations.add(new LatLng(coordination.getLatitude(), coordination.getLongitude()));
				for(Step step : leg.getStepList()){
					step.setHtmlInstruction(step.getHtmlInstruction().replace("<div", ". <div").replaceAll("\\<.*?>",""));
					instructions.add(step);
					Log.d("eX_instruction", step.getHtmlInstruction());
				}
			}

			this.customRoute.setSteps(instructions);
			if (this.customRoute.getDistance() == -1)
			{
				this.customRoute.setDistance(distance);
			}

			this.drawRouteOnMap(route);
			this.showRouteInfo();

			this.map.setPadding(0, 0, 0, this.routeInfoLayout.getHeight());
			this.map.animateCamera(Utility.getCameraUpdateBounds(this.width, this.height, (int) (this.height*0.15), locations));
		}
		else
		{
			Toast.makeText(this.getActivity(), R.string.directions_api_request_error, Toast.LENGTH_LONG).show();
		}
	}

	public void showProgressBar()
	{
		this.layout.setAlpha(0.4f);
		this.progressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar()
	{
		this.progressBar.setVisibility(View.GONE);
		this.layout.setAlpha(1);
	}

	public void slideUpFragment(ScrollView layout)
	{
		layout.setAnimation(slideUpAnimation);
		layout.setVisibility(View.VISIBLE);
		layout.startAnimation(slideUpAnimation);
	}

	public void slideDownFragment(ScrollView layout)
	{
		this.map.setPadding(0, 0, 0, 0);
		layout.setAnimation(this.slideDownAnimation);
		layout.setVisibility(View.GONE);
		layout.startAnimation(this.slideDownAnimation);
	}

	public boolean slideDownFragments()
	{
		boolean res = false;
		if (this.routeInfoLayout.getVisibility() == View.VISIBLE)
		{
			slideDownFragment(this.routeInfoLayout);
			res = true;
		}
		if (this.routeCreationLayout.getVisibility() == View.VISIBLE)
		{
			slideDownFragment(this.routeCreationLayout);
			res = true;
		}
		return res;
	}

	public void onRouteStart()
	{
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		this.slideDownFragment(this.routeInfoLayout);
		this.slideUpFragment(this.currentRouteLayout);
		this.currentRouteFragment.update(this.customRoute);
	}

	public void onRouteStop()
	{
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		this.removeRoute();
		this.slideDownFragment(this.routeInfoLayout);
		this.mFormButton.setText(R.string.start_form);
		this.mFormButton.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(R.drawable.ic_search), null, null, null);
		this.mFormButton.setOnClickListener(this.searchRouteClickListener);
	}

	public void removeRoute()
	{
		this.removePolylines();
		this.customRoute = null;
		this.route = null;
	}
}
