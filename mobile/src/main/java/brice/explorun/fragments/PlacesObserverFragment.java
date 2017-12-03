package brice.explorun.fragments;

import android.location.Location;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.R;
import brice.explorun.controllers.WikiAttractionController;
import brice.explorun.services.RouteService;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.utilities.Utility;

public abstract class PlacesObserverFragment extends Fragment
{
	protected NearbyAttractionsController nearbyAttractionsController;
	protected Location mLastLocation = new Location("");
	protected LinearLayout progressBarLayout = null;

	public PlacesObserverFragment()
	{
		this.mLastLocation.setLatitude(-1);
		this.mLastLocation.setLongitude(-1);
	}

	public void getNearbyPlaces()
	{
		float[] loc = LocationUtility.getLocationFromPreferences(this.getActivity());
		float latitude = loc[0];
		float longitude = loc[1];

		double distance = LocationUtility.distanceBetweenCoordinates(latitude, longitude, this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());

		if (Utility.isOnline(this.getActivity()) && distance > 0.1)
		{
			Log.i("eX_placesObserver", "getNearbyPlaces");
			if (this.progressBarLayout != null)
			{
				this.progressBarLayout.setVisibility(View.VISIBLE);
			}
			this.mLastLocation.setLatitude(latitude);
			this.mLastLocation.setLongitude(longitude);
			this.nearbyAttractionsController.getNearbyPlaces(this.mLastLocation);
		}
	}

	public void updatePlaces(ArrayList<Place> places, int errorsCount)
	{
		if (errorsCount > 0)
		{
			Toast.makeText(this.getActivity(), R.string.api_request_error, Toast.LENGTH_LONG).show();
		}
		if (places.size() == 0 && errorsCount == 0)
		{
			Toast.makeText(this.getActivity(), R.string.no_nearby_attraction, Toast.LENGTH_LONG).show();
		}
	}
	public abstract void updatePlacePhoto(Photo photo);
}
