package brice.explorun.models;

import java.util.ArrayList;

/**
 * Class which represents a place in our application
 */

public class Place
{
	private String placeId = ""; // Id of the place
	private String name = ""; // Name of the place

	private double latitude = -1; // Latitude of the place
	private double longitude = -1; // Longitude of the place

	private ArrayList<String> types = new ArrayList<>(); // Types of the place, used by Google

	private String iconUrl = ""; // Url of the place's icon on the map
	private Photo photo; // Photo of the place

	public String getPlaceId()
	{
		return placeId;
	}

	public void setPlaceId(String placeId)
	{
		this.placeId = placeId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public ArrayList<String> getTypes()
	{
		return types;
	}

	public void setTypes(ArrayList<String> types)
	{
		this.types = types;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

	public Photo getPhoto()
	{
		return photo;
	}

	public void setPhoto(Photo photo)
	{
		this.photo = photo;
	}

	public Place(String placeId, String name, double latitude, double longitude, ArrayList<String> types, String iconUrl, Photo photo)
	{
		this.setPlaceId(placeId);
		this.setName(name);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setTypes(types);
		this.setIconUrl(iconUrl);
		this.setPhoto(photo);
	}
}
