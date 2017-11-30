package brice.explorun.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Class which represents a place in our application
 */

public class Place implements Parcelable
{
	private String placeId = ""; // Id of the place
	private String name = ""; // Name of the place

	private double latitude = -1; // Latitude of the place
	private double longitude = -1; // Longitude of the place
	private double distance = 0; // Distance between the place and the user (in km)

	private ArrayList<String> types = new ArrayList<>(); // Types of the place, used by Google

	private String iconUrl = ""; // Url of the place's icon on the map
	private Photo photo; // Photo of the place

	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

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

	public double getDistance()
	{
		return distance;
	}

	public void setDistance(double distance)
	{
		this.distance = distance;
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

	protected Place(Parcel in)
	{
		this.placeId = in.readString();
		this.name = in.readString();
		this.latitude = in.readDouble();
		this.longitude = in.readDouble();
		this.distance = in.readDouble();
		if (in.readByte() == 0x01)
		{
			this.types = new ArrayList<>();
			in.readList(this.types, String.class.getClassLoader());
		}
		else
		{
			this.types = null;
		}
		this.iconUrl = in.readString();
		this.photo = (Photo) in.readValue(Photo.class.getClassLoader());
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.placeId);
		dest.writeString(this.name);
		dest.writeDouble(this.latitude);
		dest.writeDouble(this.longitude);
		dest.writeDouble(this.distance);
		if (this.types == null)
		{
			dest.writeByte((byte) (0x00));
		}
		else
		{
			dest.writeByte((byte) (0x01));
			dest.writeList(this.types);
		}
		dest.writeString(this.iconUrl);
		dest.writeValue(this.photo);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>()
	{
		@Override
		public Place createFromParcel(Parcel in)
		{
			return new Place(in);
		}

		@Override
		public Place[] newArray(int size)
		{
			return new Place[size];
		}
	};
}
