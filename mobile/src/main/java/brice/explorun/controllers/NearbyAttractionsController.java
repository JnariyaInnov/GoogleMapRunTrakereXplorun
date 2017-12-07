package brice.explorun.controllers;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import brice.explorun.R;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.Utility;
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
	private ArrayList<AsyncTask> asyncTasks;

	private Location location;

	private String[] types;
	private int requestsCount; // Number of API requests to do
	private int responsesCount = 0; // Number of received API responses
	private int errorsCount = 0; // Number of API requests errors
	private final int maxPlacesByType = 20;

	private ArrayList<Place> places;

	public NearbyAttractionsController(PlacesObserverFragment observer)
	{
		this.observer = observer;
		this.asyncTasks = new ArrayList<>();

		this.types = this.observer.getResources().getStringArray(R.array.places_types);
		this.requestsCount = types.length;

		this.places = new ArrayList<>();
	}

	private String getPlacesApiUrl(String type)
	{
		Uri builtUri = Uri.parse(PLACES_API_BASE_URL).buildUpon()
				.appendQueryParameter("location", this.location.getLatitude() + "," + this.location.getLongitude())
				.appendQueryParameter("radius", Integer.toString(this.RADIUS))
				.appendQueryParameter("language", Locale.getDefault().getLanguage())
				.appendQueryParameter("type", type)
				.appendQueryParameter("key", this.observer.getResources().getString(R.string.google_places_web_key))
				.build();

		return builtUri.toString();
	}

	public void getNearbyPlaces(Location location)
	{
		this.location = location;
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

	public synchronized void updatePlaces(ArrayList<Place> places)
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
					double distance = LocationUtility.distanceBetweenCoordinates(this.location.getLatitude(), this.location.getLongitude(), p1.getLatitude(), p1.getLongitude());
					p1.setDistance(distance*1000);

					this.places.add(p1);
				}
			}
		}
		else
		{
			this.errorsCount++;
		}
		if (this.responsesCount == this.requestsCount)
		{
			this.responsesCount = 0;
			this.observer.updatePlaces(this.places, this.errorsCount);
			this.errorsCount = 0;
		}
	}

	public void getPlacePhoto(Place place)
	{
		Photo photo = place.getPhoto();
		if (photo != null)
		{
			PhotoRetriever task = new PhotoRetriever(this);
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

			if (status.equals("OK"))
			{
				ArrayList<Place> places = new ArrayList<>();
				JSONArray results = response.getJSONArray("results");
				int i = 0, j = 0;
				while (i < maxPlacesByType && j < results.length())
				{
					JSONObject object = results.getJSONObject(j);

					// Get the icon of the place
					String iconUrl = object.getString("icon");

					if (!iconUrl.contains("shopping") && !iconUrl.contains("business") && !iconUrl.contains("camping"))
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
				this.updatePlaces(places);
			}
			else if (status.equals("ZERO_RESULTS"))
			{
				this.updatePlaces(new ArrayList<Place>());
			}
			else
			{
				this.updatePlaces(null);
			}
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
