package brice.explorun.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import brice.explorun.controllers.NearbyAttractionsController;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;
import brice.explorun.models.Utility;

public abstract class PlacesObserverFragment extends Fragment
{
	protected NearbyAttractionsController nearbyAttractionsController;
	protected Location mLastLocation = new Location("");
	protected LinearLayout progressBarLayout = null;

	public void getNearbyPlaces()
	{
		float[] loc = Utility.getLocationFromPreferences(this.getActivity());
		float latitude = loc[0];
		float longitude = loc[1];

		double distance = Utility.distanceBetweenCoordinates(latitude, longitude, this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());

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
	public abstract void updatePlaces(ArrayList<Place> places, boolean error);
	public abstract void updatePlacePhoto(Photo photo);
}
