package brice.explorun.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.util.Comparator;

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

	private ArrayList<Place> places;
	private NearbyAttractionsAdapter adapter;

	private NearbyAttractionsController manager;

	private LinearLayout progressBarLayout;

	private final String PLACES_KEY = "places";

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

		this.manager = new NearbyAttractionsController(this, this.mGoogleApiClient);

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
		this.manager.cancelAllAsyncTasks();
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

	public void getNearbyPlaces()
	{
		if (Utility.isOnline(this.getActivity()))
		{
			this.progressBarLayout.setVisibility(View.VISIBLE);
		}
		this.manager.getNearbyPlaces(true);
	}

	public void updatePlaces(ArrayList<Place> places, boolean error)
	{
		if (error)
		{
			Toast.makeText(this.getActivity(), R.string.api_request_error, Toast.LENGTH_LONG).show();
		}
		if (places.size() > 0)
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
			this.manager.getPlacePhoto(p);
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
		MapFragment fragment = new MapFragment();
		fragment.setArguments(args);
		// Setting the new fragment in MainActivity
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getSupportActionBar().setTitle(activity.getResources().getString(R.string.app_name));
		activity.selectItem(activity.getNavigationView().getMenu().getItem(0), args);
	}
}
