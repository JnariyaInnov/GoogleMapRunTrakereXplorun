package brice.explorun.controllers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import brice.explorun.models.Place;
import brice.explorun.models.Utility;

/**
 * Created by germain on 11/11/17.
 */

public class ParcoursController {
    private final int nbIterations = 100;
    private ArrayList<Place> places;
    private float[] userLocation;
    private float minKM;
    private float maxKM;


    public ParcoursController(ArrayList<Place> places, float[] userLocation, float minKM, float maxKM){
        this.places = places;
        this.userLocation = userLocation;
        this.minKM = minKM;
        this.maxKM = maxKM;
    }

    public ArrayList<Place> generateParcours(){
        ArrayList<Place> validPlaces = new ArrayList<Place>();
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
        while(selectedPlaces == null && i++ < nbIterations){
            selectedPlaces = getParcours();
        }
        return selectedPlaces;
    }

    private  ArrayList<Place> getParcours(){
        ArrayList<Place> res = new ArrayList<Place>();
        ArrayList<Place> placesLeft = new ArrayList<>(places);
        Random r = new Random();
        //Select a random place
        Place curPlace = placesLeft.get(r.nextInt(placesLeft.size()));
        Place lastPlace = curPlace;
        res.add(curPlace);
        placesLeft.remove(curPlace);
        //Init distance to distance to selected place
        double total_distance = distanceToUserLocation(lastPlace);

        //While totalDistance of the parcours is less than minKm
        while((total_distance + distanceToUserLocation(lastPlace) < minKM) && placesLeft.size() > 0){
            //Select a random place and add it to current parcours
            curPlace = placesLeft.get(r.nextInt(placesLeft.size()));
            total_distance += distanceBetweenPlace(lastPlace, curPlace);
            res.add(curPlace);
            placesLeft.remove(curPlace);
            lastPlace = curPlace;
        }
        //If parcours is valid, return it
        if(total_distance > minKM && total_distance < maxKM){
            return res;
        }
        //Random gone wrong, better luck next time
        else if (total_distance > maxKM){
            return null;
        }
        //No parcours of this length can be created with current places list
        else {
            Log.e("eX_parcours", "Not enough places to do a parcours of this length");
            return null;
        }

    }

    private double distanceToUserLocation(Place p){
        return Utility.distanceBetweenCoordinates(userLocation[0], userLocation[1], p.getLatitude(), p.getLongitude());
    }

    private double distanceBetweenPlace(Place p1, Place p2){
        return Utility.distanceBetweenCoordinates(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }
}
