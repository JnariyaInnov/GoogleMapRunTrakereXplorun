package brice.explorun.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.libraries.places.api.Places;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import brice.explorun.activities.MainActivity;
import brice.explorun.adapters.NearbyAttractionsAdapter;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.R;
import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.services.RouteService;

public class NearbyAttractionsFragment extends PlacesObserverFragment
{
	private List<String> types;
	private ArrayList<Place> places;
	private NearbyAttractionsAdapter adapter;

	private final String PLACES_KEY = "places";

	private BroadcastReceiver locReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (!RouteService.isStarted)
			{
				getNearbyPlaces();
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view =  inflater.inflate(R.layout.fragment_nearby_attractions, container, false);

		// Initialize Places.
		Places.initialize(this.getActivity().getApplicationContext(), getResources().getString(R.string.google_places_android_key));

		// Create a new Places client instance.
		this.placesClient = Places.createClient(this.getActivity());

		this.nearbyAttractionsController = new NearbyAttractionsController(this, this.placesClient);

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

		if (savedInstanceState == null)
		{
			getNearbyPlaces();
		}

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getConnectivityStatusHandler().updateNoNetworkLabel();
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
		super.onSaveInstanceState(outBundle);
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
