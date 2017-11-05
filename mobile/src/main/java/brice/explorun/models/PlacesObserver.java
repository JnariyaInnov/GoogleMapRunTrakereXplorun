package brice.explorun.models;

import java.util.ArrayList;

public interface PlacesObserver
{
	void updatePlaces(ArrayList<Place> places);
	void updatePlacePhoto(Photo photo);
}
