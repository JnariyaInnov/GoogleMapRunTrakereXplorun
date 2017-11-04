package brice.explorun.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.ArrayList;
import java.util.Locale;

import brice.explorun.Utility;
import brice.explorun.models.CustomRequestQueue;
import brice.explorun.models.NearbyAttractionsAdapter;
import brice.explorun.models.Place;
import brice.explorun.models.PlacesObserver;
import brice.explorun.observables.NearbyAttractionsCallback;
import brice.explorun.R;

public class NearbyAttractionsFragment extends Fragment implements PlacesObserver
{
	private final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	private final int RADIUS = 5000; // 5km around user's location

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
			this.adapter.addAll(places);
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
		}
	}
}
