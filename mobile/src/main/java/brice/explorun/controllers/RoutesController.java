package brice.explorun.controllers;

import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.request.DirectionDestinationRequest;
import com.akexorcist.googledirection.request.DirectionRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

import brice.explorun.R;
import brice.explorun.fragments.MapFragment;
import brice.explorun.models.Place;
import brice.explorun.models.Utility;

public class RoutesController implements DirectionCallback
{
    private final int nbIterations = 100;
    private final int nbPlaces = 3;
    private ArrayList<Place> places;
    private float[] userLocation;

    private MapFragment fragment;

	public void setPlaces(ArrayList<Place> places)
	{
		this.places = places;
	}

	public void setUserLocation(float[] userLocation)
	{
		this.userLocation = userLocation;
	}

	public RoutesController(MapFragment fragment, ArrayList<Place> places, float[] userLocation){
	    this.fragment = fragment;
        this.places = places;
        this.userLocation = userLocation;
    }

    public ArrayList<Place> generateRoute(double minKM, double maxKM){
		Log.i("eX_route", "Generating route");
        ArrayList<Place> validPlaces = new ArrayList<>();
        ArrayList<Place> selectedPlaces = null;
        //Remove places that are too far away
        for (Place p : places) {
            if(2.0 * distanceToUserLocation(p) < maxKM){
                validPlaces.add(p);
            }
        }
        //Begin to select places
        places = validPlaces;
        int i = 0;
        while(selectedPlaces == null && i++ < this.nbIterations){
            selectedPlaces = getRoute(new ArrayList<>(this.places), minKM, maxKM);
        }
        return selectedPlaces;
    }

    public void printRoute(ArrayList<Place> route){
        String routeString = "";
        for(Place p : route){
            routeString += p.getName() + " => ";
        }
        Log.i("eX_route", routeString.substring(0, routeString.length() - 4));
    }

    private  ArrayList<Place> getRoute(ArrayList<Place> placesLeft, double minKM, double maxKM){
        ArrayList<Place> res = new ArrayList<>();
        Random r = new Random();
        //Select a random place
        Place curPlace = placesLeft.get(r.nextInt(placesLeft.size()));
        Place lastPlace = curPlace;
        res.add(curPlace);
        placesLeft.remove(curPlace);
        //Init distance to distance to selected place
        double totalDistance = distanceToUserLocation(lastPlace);

        //While totalDistance of the route is less than minKm
        while((totalDistance + distanceToUserLocation(lastPlace) < minKM) && placesLeft.size() > 0){
            //Select a random place near lastPlace and add it to current route
            ArrayList<Place> nearestPlaces = getNearestPlaces(placesLeft, lastPlace);
            curPlace = nearestPlaces.get(r.nextInt(nearestPlaces.size()));
            totalDistance += distanceBetweenPlace(lastPlace, curPlace);
            res.add(curPlace);
            placesLeft.remove(curPlace);
            lastPlace = curPlace;
        }

        totalDistance += distanceToUserLocation(lastPlace);

        //If route is valid, return it
        if(totalDistance >= minKM && totalDistance <= maxKM){
            return res;
        }
        //Random gone wrong, better luck next time
        else if (totalDistance > maxKM){
            Log.d("eX_route", "Route is too long, trying again");
            return null;
        }
        //No route of this length can be created with current places list
        else {
            Log.d("eX_route", "Route is too short, trying again");
            return null;
        }

    }

    private ArrayList<Place> getNearestPlaces(ArrayList<Place> placesLeft, Place lastPlace){
        //Compute distance to current place
        for(Place p : placesLeft){
            p.setDistance(distanceBetweenPlace(lastPlace, p));
        }
        //Sort placesLeft by distance
        Collections.sort(placesLeft, new Comparator<Place>(){
            public int compare(Place p1, Place p2) {
                return Double.compare(p1.getDistance(), p2.getDistance());
            }
        });
        //Return subset of places (0 to nbPlaces - 1)
        if(placesLeft.size() > this.nbPlaces){
            return new ArrayList<>(placesLeft.subList(0, this.nbPlaces));
        }
        else{
            return placesLeft;
        }
    }

    private double distanceToUserLocation(Place p){
        return Utility.distanceBetweenCoordinates(this.userLocation[0], this.userLocation[1], p.getLatitude(), p.getLongitude());
    }

    private double distanceBetweenPlace(Place p1, Place p2){
        return Utility.distanceBetweenCoordinates(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }

    public void getRouteFromAPI(ArrayList<Place> route)
	{
		getDirectionRequest(route).execute(this);
	}

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody)
    {
        if (direction.isOK())
        {
            this.fragment.drawRouteOnMap(direction.getRouteList().get(0	));
        }
        else
        {
        	Log.e("eX_route", "Error with Direction API request: " + direction.getErrorMessage());
            this.fragment.drawRouteOnMap(null);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t)
    {
    	Log.e("eX_route", "Error with Direction API request: " + t.getMessage());
        this.fragment.drawRouteOnMap(null);
    }

    private DirectionRequest getDirectionRequest(ArrayList<Place> route)
    {
        LatLng userLoc = new LatLng(this.userLocation[0], this.userLocation[1]);

        DirectionDestinationRequest request =
                        GoogleDirection.withServerKey(this.fragment.getResources().getString(R.string.google_directions_web_key))
                        .from(userLoc);

        for (Place place: route)
		{
			request.and(new LatLng(place.getLatitude(), place.getLongitude()));
		}

		return request.to(userLoc)
				.transportMode(TransportMode.WALKING)
				.language(Locale.getDefault().getLanguage())
				.unit(Unit.METRIC)
				.avoid(AvoidType.FERRIES)
				.avoid(AvoidType.HIGHWAYS);
    }
}
