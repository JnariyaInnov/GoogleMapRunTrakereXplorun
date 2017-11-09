package brice.explorun.fragments;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.util.ArrayList;

import brice.explorun.R;
import brice.explorun.models.Photo;
import brice.explorun.models.Place;

public abstract class PlacesObserverFragment extends Fragment
{
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
