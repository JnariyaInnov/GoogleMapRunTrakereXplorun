package brice.explorun.observables;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Locale;

import brice.explorun.R;
import brice.explorun.Utility;
import brice.explorun.models.CustomRequestQueue;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.fragments.PlacesObserverFragment;
import brice.explorun.models.PlacesPhotoTask;

public class NearbyAttractionsManager
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

	private NearbyAttractionsCallback nearbyAttractionsCallback;

	private ArrayList<Place> places;

	public NearbyAttractionsManager(PlacesObserverFragment observer, GoogleApiClient googleApiClient)
	{
		this.observer = observer;
		this.mGoogleApiClient = googleApiClient;
		this.asyncTasks = new ArrayList<>();

		// Retrieve user's location in the SharedPreferences
		SharedPreferences prefs = this.observer.getActivity().getPreferences(Context.MODE_PRIVATE);
		this.latitude = prefs.getFloat("latitude", -1);
		this.longitude = prefs.getFloat("longitude", -1);

		this.nearbyAttractionsCallback = new NearbyAttractionsCallback(this);

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
				JsonObjectRequest request = new JsonObjectRequest(
						Request.Method.GET,
						url, null,
						this.nearbyAttractionsCallback,
						this.nearbyAttractionsCallback
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
			PlacesPhotoTask task = new PlacesPhotoTask(this, this.mGoogleApiClient);
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
}
