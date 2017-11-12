package brice.explorun.controllers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import brice.explorun.models.Place;
import brice.explorun.models.Utility;

/**
 * Created by germain on 11/11/17.
 */

public class ParcoursController {
    private final int nbIterations = 100;
    private final int nbPlaces = 3;
    private ArrayList<Place> places;
    private float[] userLocation;


    public ParcoursController(ArrayList<Place> places, float[] userLocation){
        this.places = places;
        this.userLocation = userLocation;
    }

    public ArrayList<Place> generateParcours(double minKM, double maxKM){
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
            selectedPlaces = getParcours(new ArrayList<Place>(places), minKM, maxKM);
        }
        return selectedPlaces;
    }

    public void printParcours(ArrayList<Place> parcours){
        String parcoursString = "";
        for(Place p : parcours){
            parcoursString += p.getName() + " => ";
        }
        Log.i("eX_parcours", parcoursString.substring(0, parcoursString.length() - 4));
    }

    private  ArrayList<Place> getParcours(ArrayList<Place> placesLeft, double minKM, double maxKM){
        ArrayList<Place> res = new ArrayList<Place>();
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
            //Select a random place near lastPlace and add it to current parcours
            ArrayList<Place> nearestPlaces = getNearestPlaces(placesLeft, lastPlace);
            curPlace = nearestPlaces.get(r.nextInt(nearestPlaces.size()));
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
            Log.d("eX_parcours", "Parcours is too long, trying again");
            return null;
        }
        //No parcours of this length can be created with current places list
        else {
            Log.d("eX_parcours", "Parcours is too short, trying again");
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
        //Return subset of places (0 to nbPlace - 1)
        if(placesLeft.size() > nbPlaces){
            return new ArrayList<Place>(placesLeft.subList(0, nbPlaces));
        }
        else{
            return placesLeft;
        }
    }

    private double distanceToUserLocation(Place p){
        return Utility.distanceBetweenCoordinates(userLocation[0], userLocation[1], p.getLatitude(), p.getLongitude());
    }

    private double distanceBetweenPlace(Place p1, Place p2){
        return Utility.distanceBetweenCoordinates(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }
}
