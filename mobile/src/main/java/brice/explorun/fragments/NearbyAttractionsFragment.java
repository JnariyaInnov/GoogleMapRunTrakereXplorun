package brice.explorun.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import brice.explorun.Utility;
import brice.explorun.models.CustomRequestQueue;
import brice.explorun.models.NearbyAttractionsAdapter;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.models.PlacesObserver;
import brice.explorun.models.PlacesPhotoTask;
import brice.explorun.observables.NearbyAttractionsCallback;
import brice.explorun.R;

public class NearbyAttractionsFragment extends Fragment implements PlacesObserver, GoogleApiClient.OnConnectionFailedListener
{
	private final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	private final int RADIUS = 5000; // 5km around user's location

	private GoogleApiClient mGoogleApiClient;
	private ArrayList<AsyncTask> asyncTasks;

	private float latitude;
	private float longitude;

	private String[] types;
	private ArrayList<Place> places;
	private NearbyAttractionsAdapter adapter;

	private NearbyAttractionsCallback nearbyAttractionsCallback;
	private int requestsCount; // Number of API requests to do
	private int responsesCount = 0; // Number of received API responses

	private LinearLayout progressBarLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view =  inflater.inflate(R.layout.fragment_nearby_attractions, container, false);

		// Retrieve user's location in the SharedPreferences
		SharedPreferences prefs = this.getActivity().getPreferences(Context.MODE_PRIVATE);
		this.latitude = prefs.getFloat("latitude", -1);
		this.longitude = prefs.getFloat("longitude", -1);

		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null) {
			this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
					.addApi(Places.GEO_DATA_API)
					.addOnConnectionFailedListener(this)
					.build();

		}

		this.asyncTasks = new ArrayList<>();

		if (this.latitude != -1 && this.longitude != -1)
		{
			this.nearbyAttractionsCallback = new NearbyAttractionsCallback(this);

			// Setting up the list of places
			this.places = new ArrayList<>();
			ListView list = view.findViewById(R.id.list_nearby_attractions);
			this.adapter = new NearbyAttractionsAdapter(this.getActivity(), places);
			list.setAdapter(this.adapter);

			this.types = this.getResources().getStringArray(R.array.places_types);
			this.requestsCount = types.length;

			this.progressBarLayout = view.findViewById(R.id.progress_layout);

			sendRequests();
		}

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
		for (AsyncTask task: this.asyncTasks)
		{
			task.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{

	}

	public String getPlacesApiUrl(String type)
	{
		Uri builtUri = Uri.parse(PLACES_API_BASE_URL).buildUpon()
				.appendQueryParameter("location", this.latitude + "," + this.longitude)
				.appendQueryParameter("radius", Integer.toString(this.RADIUS))
				.appendQueryParameter("language", Locale.getDefault().getLanguage())
				.appendQueryParameter("type", type)
				.appendQueryParameter("key", getResources().getString(R.string.google_places_web_key))
				.build();

		return builtUri.toString();
	}

	public void sendRequests()
	{
		if (Utility.isOnline(this.getActivity()))
		{
			this.progressBarLayout.setVisibility(View.VISIBLE);

			this.adapter.clear();

			for (String type : this.types)
			{
				String url = getPlacesApiUrl(type);
				JsonObjectRequest request = new JsonObjectRequest(
						Request.Method.GET,
						url, null,
						this.nearbyAttractionsCallback,
						this.nearbyAttractionsCallback
				);

				CustomRequestQueue.getInstance(this.getActivity()).getRequestQueue().add(request);
			}
		}
	}

	public synchronized void updatePlaces(ArrayList<Place> places)
	{
		this.responsesCount++;
		if (places != null)
		{
			// We add the place only if it doesn't already exist
			for (Place p1: places)
			{
				boolean found = false;
				int i = 0;
				while (!found && i < this.places.size())
				{
					Place p2 = this.places.get(i);
					if (p1.getPlaceId().equals(p2.getPlaceId()))
					{
						found = true;
					}
					else
					{
						i++;
					}
				}
				if (!found)
				{
					// Compute distance between the user and the place
					double distance = Utility.distanceBetweenCoordinates(this.latitude, this.longitude, p1.getLatitude(), p1.getLongitude());
					p1.setDistance(distance);

					this.adapter.add(p1);
				}
			}
			this.adapter.notifyDataSetChanged();
		}
		else
		{
			Toast.makeText(this.getActivity(), R.string.api_request_error, Toast.LENGTH_LONG).show();
		}
		if (this.responsesCount == this.requestsCount)
		{
			this.progressBarLayout.setVisibility(View.GONE);
			this.responsesCount = 0;
			this.adapter.sort(new Comparator<Place>()
			{
				@Override
				public int compare(Place p1, Place p2)
				{
					return Double.compare(p1.getDistance(), p2.getDistance());
				}
			});

			this.getPlacesImages();
		}
	}

	public void getPlacesImages()
	{
		for (Place p: this.places)
		{
			PlacesPhotoTask task = new PlacesPhotoTask(this, this.mGoogleApiClient);
			this.asyncTasks.add(task);
			task.execute(p.getPhoto());
		}
	}

	public synchronized void updatePlacePhoto(Photo photo)
	{
		this.adapter.notifyDataSetChanged();
	}
}
