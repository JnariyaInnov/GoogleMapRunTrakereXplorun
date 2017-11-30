package brice.explorun.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import brice.explorun.utilities.SportUtility;

public class FirebaseRoute
{
	private Date date;
	private int sportType = SportUtility.WALKING;
	private double distance = 0;
	private long duration = 0;
	private LatLng startPosition;
	private ArrayList<LatLng> places;

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public int getSportType()
	{
		return sportType;
	}

	public void setSportType(int sportType)
	{
		this.sportType = sportType;
	}

	public double getDistance()
	{
		return distance;
	}

	public void setDistance(double distance)
	{
		this.distance = distance;
	}

	public long getDuration()
	{
		return duration;
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
	}

	public LatLng getStartPosition()
	{
		return startPosition;
	}

	public void setStartPosition(LatLng startPosition)
	{
		this.startPosition = startPosition;
	}

	public ArrayList<LatLng> getPlaces()
	{
		return places;
	}

	public void setPlaces(ArrayList<LatLng> places)
	{
		this.places = places;
	}

	public FirebaseRoute(Date date, int sportType, double distance, long duration, LatLng startPosition, ArrayList<LatLng> places)
	{
		this.date = date;
		this.sportType = sportType;
		this.distance = distance;
		this.duration = duration;
		this.startPosition = startPosition;
		this.places = places;
	}
}
