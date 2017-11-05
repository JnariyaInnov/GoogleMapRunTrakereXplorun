package brice.explorun.observables;

import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;

import brice.explorun.R;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.models.PlacesObserver;

public class NearbyAttractionsCallback extends Observable implements Response.Listener<JSONObject>, Response.ErrorListener
{
	private PlacesObserver observer;

	public NearbyAttractionsCallback(PlacesObserver observer)
	{
		this.observer = observer;
	}

	@Override
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
						Photo photo = new Photo(placeId, 0, 0);
						if (object.has("photos")) // We check if there is a photo for the place
						{
							JSONArray photos = object.getJSONArray("photos");
							JSONObject photoObject = photos.getJSONObject(0);
							photo.setWidth(photoObject.getInt("width"));
							photo.setHeight(photoObject.getInt("height"));
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
			this.observer.updatePlaces(places);
		}
		catch (JSONException e)
		{
			this.observer.updatePlaces(null);
		}
	}

	@Override
	public void onErrorResponse(VolleyError error)
	{
		this.observer.updatePlaces(null);
	}
}
