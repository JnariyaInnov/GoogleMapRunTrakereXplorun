package brice.explorun.fragments;

import android.support.v4.app.Fragment;

import java.util.ArrayList;

import brice.explorun.models.Photo;
import brice.explorun.models.Place;

public abstract class PlacesObserverFragment extends Fragment
{
	public abstract void updatePlaces(ArrayList<Place> places, int errorsCount);
	public abstract void updatePlacePhoto(Photo photo);
}
