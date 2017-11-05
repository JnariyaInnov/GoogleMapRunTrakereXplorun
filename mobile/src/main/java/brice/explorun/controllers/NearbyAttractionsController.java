package brice.explorun.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import brice.explorun.R;
import brice.explorun.models.Utility;
import brice.explorun.models.CustomRequestQueue;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.fragments.PlacesObserverFragment;
import brice.explorun.services.PhotoRetriever;

public class NearbyAttractionsController
{
	private final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	private final int RADIUS = 5000; // 5km around user's location

	private PlacesObserverFragment observer;
	private GoogleApiClient mGoogleApiClient;
	private ArrayList<AsyncTask> asyncTasks;

	private float latitude;
	private float longitude;

	private String[] types;
	private int requestsCount; // Number of API requests to do
	private int responsesCount = 0; // Number of received API responses

	private ArrayList<Place> places;

	public NearbyAttractionsController(PlacesObserverFragment observer, GoogleApiClient googleApiClient)
	{
		this.observer = observer;
		this.mGoogleApiClient = googleApiClient;
		this.asyncTasks = new ArrayList<>();

		// Retrieve user's location in the SharedPreferences
		SharedPreferences prefs = this.observer.getActivity().getPreferences(Context.MODE_PRIVATE);
		this.latitude = prefs.getFloat("latitude", -1);
		this.longitude = prefs.getFloat("longitude", -1);
		this.types = this.observer.getResources().getStringArray(R.array.places_types);
		this.requestsCount = types.length;

		this.places = new ArrayList<>();
	}

	private String getPlacesApiUrl(String type)
	{
		Uri builtUri = Uri.parse(PLACES_API_BASE_URL).buildUpon()
				.appendQueryParameter("location", this.latitude + "," + this.longitude)
				.appendQueryParameter("radius", Integer.toString(this.RADIUS))
				.appendQueryParameter("language", Locale.getDefault().getLanguage())
				.appendQueryParameter("type", type)
				.appendQueryParameter("key", this.observer.getResources().getString(R.string.google_places_web_key))
				.build();

		return builtUri.toString();
	}

	public void getNearbyPlaces()
	{
		if (Utility.isOnline(this.observer.getActivity()))
		{
			this.places.clear();

			for (String type : this.types)
			{
				String url = getPlacesApiUrl(type);
				final NearbyAttractionsController _this = this;
				JsonObjectRequest request = new JsonObjectRequest(
						Request.Method.GET,
						url,
						null,
						new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								_this.onResponse(response);
							}
						},
						new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								_this.onErrorResponse(error);
							}
						}
				);

				CustomRequestQueue.getInstance(this.observer.getActivity()).getRequestQueue().add(request);
			}
		}
	}

	public void updatePlaces(ArrayList<Place> places)
	{
		this.responsesCount++;
		if (places != null)
		{
			// We add the place only if it doesn't already exist
			for (Place p1 : places)
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

					this.places.add(p1);
				}
			}
		}
		else
		{
			Toast.makeText(this.observer.getActivity(), R.string.api_request_error, Toast.LENGTH_LONG).show();
		}
		if (this.responsesCount == this.requestsCount)
		{
			this.responsesCount = 0;
			this.observer.updatePlaces(this.places);
		}
	}

	public void getPlacePhoto(Place place)
	{
		Photo photo = place.getPhoto();
		if (photo != null)
		{
			PhotoRetriever task = new PhotoRetriever(this, this.mGoogleApiClient);
			this.asyncTasks.add(task);
			task.execute(photo);
		}
	}

	public synchronized void updatePlacePhoto(Photo photo)
	{
		this.observer.updatePlacePhoto(photo);
	}

	public void cancelAllAsyncTasks()
	{
		for (AsyncTask task: this.asyncTasks)
		{
			task.cancel(true);
		}
	}

	public void onResponse(JSONObject response)
	{
		try
		{
			String status = response.getString("status");
			ArrayList<Place> places = new ArrayList<>();
			if (status.equals("OK"))
			{
				JSONArray results = response.getJSONArray("results");
				int i = 0, j = 0;
				while (i < 5 && j < results.length())
				{
					JSONObject object = results.getJSONObject(j);

					// Get the icon of the place
					String iconUrl = object.getString("icon");

					if (!iconUrl.contains("shopping"))
					{
						// Get the location of the place
						JSONObject geometry = object.getJSONObject("geometry");
						JSONObject location = geometry.getJSONObject("location");
						double latitude = location.getDouble("lat");
						double longitude = location.getDouble("lng");

						// Get the name of the place
						String name = object.getString("name");

						// Get the id of the place
						String placeId = object.getString("place_id");

						// Get the types of the place
						ArrayList<String> types = new ArrayList<>();
						JSONArray typesArray = object.getJSONArray("types");
						for (int k = 0; k < typesArray.length(); k++)
						{
							types.add(typesArray.getString(k));
						}

						// Get the size of a photo of the place
						Photo photo = null;
						if (object.has("photos")) // We check if there is a photo for the place
						{
							photo = new Photo(placeId);
						}

						// Store it in a place object
						Place place = new Place(placeId, name, latitude, longitude, types, iconUrl, photo);

						// Add the place into the list
						places.add(place);

						i++;
					}
					j++;
				}
			}
			this.updatePlaces(places);
		}
		catch (JSONException e)
		{
			this.updatePlaces(null);
		}
	}

	public void onErrorResponse(VolleyError error)
	{
		this.updatePlaces(null);
	}
}
