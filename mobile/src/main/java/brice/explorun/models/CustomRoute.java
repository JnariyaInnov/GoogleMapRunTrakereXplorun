package brice.explorun.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.akexorcist.googledirection.model.Step;

import java.util.ArrayList;

public class CustomRoute implements Parcelable
{
	private int sportType;
	private float distance = 0;
	private ArrayList<Place> places;
	private float rating = -1;
	private ArrayList<Step> steps;

	public int getSportType()
	{
		return sportType;
	}

	public void setSportType(int sportType)
	{
		this.sportType = sportType;
	}

	public float getDistance()
	{
		return distance;
	}

	public void setDistance(float distance)
	{
		this.distance = distance;
	}

	public ArrayList<Place> getPlaces()
	{
		return places;
	}

	public void setPlaces(ArrayList<Place> places)
	{
		this.places = places;
	}

	public float getRating()
	{
		return rating;
	}

	public void setRating(float rating)
	{
		this.rating = rating;
	}

	public ArrayList<Step> getSteps()
	{
		return steps;
	}

	public void setSteps(ArrayList<Step> steps)
	{
		this.steps = steps;
	}

	public CustomRoute(int sportType, ArrayList<Place> places)
	{
		this.sportType = sportType;
		this.places = places;
	}

	public CustomRoute(int sportType, float distance, ArrayList<Place> places, float rating)
	{
		this(sportType, places);
		this.distance = distance;
		this.rating = rating;
	}

	protected CustomRoute(Parcel in)
	{
		sportType = in.readInt();
		distance = in.readFloat();
		if (in.readByte() == 0x01)
		{
			places = new ArrayList<>();
			in.readList(places, Place.class.getClassLoader());
		}
		else
		{
			places = null;
		}
		rating = in.readFloat();
		if (in.readByte() == 0x01)
		{
			steps = new ArrayList<>();
			in.readList(steps, Step.class.getClassLoader());
		}
		else
		{
			steps = null;
		}
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(sportType);
		dest.writeFloat(distance);
		if (places == null)
		{
			dest.writeByte((byte) (0x00));
		}
		else
		{
			dest.writeByte((byte) (0x01));
			dest.writeList(places);
		}
		dest.writeFloat(rating);
		if (steps == null)
		{
			dest.writeByte((byte) (0x00));
		}
		else
		{
			dest.writeByte((byte) (0x01));
			dest.writeList(steps);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<CustomRoute> CREATOR = new Parcelable.Creator<CustomRoute>()
	{
		@Override
		public CustomRoute createFromParcel(Parcel in) {
			return new CustomRoute(in);
		}

		@Override
		public CustomRoute[] newArray(int size) {
			return new CustomRoute[size];
		}
	};
}