package brice.explorun.controllers;

import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
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
import brice.explorun.models.CustomRoute;
import brice.explorun.models.Position;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.models.Place;

public class RoutesController implements DirectionCallback
{
    private final int nbIterations = 100;
    private final int nbPlaces = 5;
    private final int maxWaypoints = 23;
    private final int minWaypoint = 2;
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
	    //Transforming min and maxKm to take into account
        // difference between theoretical distance and real distance
        minKM = minKM*0.7;
        maxKM = maxKM*0.7;
		Log.i("eX_route", "Generating route");
        ArrayList<Place> validPlaces = new ArrayList<>();
        ArrayList<Place> selectedPlaces = null;
        //Remove places that are too far away
        for (Place p : this.places) {
            if(2.0 * distanceToUserLocation(p) < maxKM){
                validPlaces.add(p);
            }
        }

        if (validPlaces.size() > 0)
		{
			int i = 0;
			while (selectedPlaces == null && i++ < this.nbIterations)
			{
				selectedPlaces = getRoute(new ArrayList<>(validPlaces), minKM, maxKM);
			}
		}
        return selectedPlaces;
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
        while(((totalDistance + distanceToUserLocation(lastPlace) < minKM) && placesLeft.size() > 0) || (res.size() < minWaypoint)){
            //Select a random place near lastPlace and add it to current route
            ArrayList<Place> nearestPlaces = getNearestPlaces(placesLeft, lastPlace);
            if (nearestPlaces.size() > 0)
			{
				curPlace = nearestPlaces.get(r.nextInt(nearestPlaces.size()));
				totalDistance += distanceBetweenPlace(lastPlace, curPlace);
				res.add(curPlace);
				placesLeft.remove(curPlace);
				lastPlace = curPlace;
			}
        }

        totalDistance += distanceToUserLocation(lastPlace);

        //If route is valid
        if(totalDistance >= minKM && totalDistance <= maxKM){
            //If less than 23 waypoints, returns it
            if(res.size() < maxWaypoints){
                return res;
            }
            //Else, request won't work, aborting this route
            else{
                return null;
            }
        }
        //Random gone wrong, better luck next time
        else if (totalDistance > maxKM){
            Log.d("eX_route", "Route is too long, trying again (" + totalDistance + " > " + maxKM + ")");
            return null;
        }
        //No route of this length can be created with current places list
        else {
            Log.d("eX_route", "Route is too short, trying again (" + totalDistance + " < " + minKM + ")");
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
        //If there are not enough placesLeft, we return the list as is
        else{
            return placesLeft;
        }
    }

    private double distanceToUserLocation(Place p){
        return LocationUtility.distanceBetweenCoordinates(this.userLocation[0], this.userLocation[1], p.getLatitude(), p.getLongitude());
    }

    private double distanceBetweenPlace(Place p1, Place p2){
        return LocationUtility.distanceBetweenCoordinates(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }

    public void getRouteFromAPI(CustomRoute route)
	{
		getDirectionRequest(route).execute(this);
	}

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody)
    {
        if (direction.isOK())
        {
            this.fragment.onDirectionAPIResponse(direction.getRouteList().get(0));
        }
        else
        {
        	Log.e("eX_route", "Success - Error with Direction API request: " + direction.getErrorMessage());
            this.fragment.onDirectionAPIResponse(null);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t)
    {
    	Log.e("eX_route", "Failure - Error with Direction API request: " + t.getMessage());
        this.fragment.onDirectionAPIResponse(null);
    }

    private DirectionRequest getDirectionRequest(CustomRoute route)
    {
    	Position pos = route.getStartPosition();
		LatLng userLoc = new LatLng(pos.getLatitude(), pos.getLongitude());

        DirectionDestinationRequest request =
                        GoogleDirection.withServerKey(this.fragment.getResources().getString(R.string.google_directions_web_key))
                        .from(userLoc);

        for (Place place: route.getPlaces())
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
