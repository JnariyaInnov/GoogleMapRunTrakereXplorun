package brice.explorun.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import brice.explorun.activities.MainActivity;
import brice.explorun.models.Utility;
import brice.explorun.adapters.NearbyAttractionsAdapter;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.R;
import brice.explorun.controllers.NearbyAttractionsController;

public class NearbyAttractionsFragment extends PlacesObserverFragment implements GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient mGoogleApiClient;

	private List<String> types;
	private ArrayList<Place> places;
	private NearbyAttractionsAdapter adapter;

	private final String PLACES_KEY = "places";

	private BroadcastReceiver locReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			getNearbyPlaces();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view =  inflater.inflate(R.layout.fragment_nearby_attractions, container, false);

		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null)
		{
			this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
					.addApi(Places.GEO_DATA_API)
					.addOnConnectionFailedListener(this)
					.build();

		}

		this.nearbyAttractionsController = new NearbyAttractionsController(this);

		this.types = Arrays.asList(getResources().getStringArray(R.array.places_types));

		// Setting up the list of places
		if (savedInstanceState != null)
		{
			this.places = savedInstanceState.getParcelableArrayList(this.PLACES_KEY);
		}
		else
		{
			this.places = new ArrayList<>();
		}

		ListView list = view.findViewById(R.id.list_nearby_attractions);
		this.adapter = new NearbyAttractionsAdapter(this.getActivity(), places);
		list.setAdapter(this.adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				viewPlaceOnMap(places.get(i));
			}
		});

		IntentFilter filter = new IntentFilter("ex_location");
		getActivity().registerReceiver(this.locReceiver, filter);

		this.progressBarLayout = view.findViewById(R.id.progress_layout);

		getNearbyPlaces();

		return view;
	}

	public void onStart()
	{
		super.onStart();
		if (!this.mGoogleApiClient.isConnected())
		{
			this.mGoogleApiClient.connect();
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getConnectivityStatusHandler().updateNoNetworkLabel();
	}

	public void onStop()
	{
		if (this.mGoogleApiClient.isConnected())
		{
			this.mGoogleApiClient.disconnect();
		}
		super.onStop();
	}

	public void onDestroy()
	{
		this.nearbyAttractionsController.cancelAllAsyncTasks();
		this.getActivity().unregisterReceiver(this.locReceiver);
		super.onDestroy();
	}

	public void onSaveInstanceState(Bundle outBundle)
	{
		outBundle.putParcelableArrayList(this.PLACES_KEY, this.places);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{

	}

	public void updatePlaces(ArrayList<Place> places, int errorsCount)
	{
		super.updatePlaces(places, errorsCount);
		if (errorsCount < this.types.size())
		{
			this.adapter.clear();
		}
		this.adapter.addAll(places);

		this.adapter.sort(new Comparator<Place>()
		{
			@Override
			public int compare(Place p1, Place p2)
			{
				return Double.compare(p1.getDistance(), p2.getDistance());
			}
		});

		this.adapter.notifyDataSetChanged();

		this.progressBarLayout.setVisibility(View.GONE);

		this.getPlacesPhotos();
	}

	public void getPlacesPhotos()
	{
		for (Place p: this.places)
		{
			this.nearbyAttractionsController.getPlacePhoto(p);
		}
	}

	public synchronized void updatePlacePhoto(Photo photo)
	{
		this.adapter.notifyDataSetChanged();
	}

	public void viewPlaceOnMap(Place place)
	{
		Bundle args = new Bundle();
		args.putDouble("latitude", place.getLatitude());
		args.putDouble("longitude", place.getLongitude());
		args.putParcelableArrayList("places", places);
		// Setting the new fragment in MainActivity
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getSupportActionBar().setTitle(activity.getResources().getString(R.string.app_name));
		activity.selectItem(activity.getNavigationView().getMenu().getItem(0), args);
	}
}
