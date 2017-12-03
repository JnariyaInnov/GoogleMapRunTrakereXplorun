package brice.explorun.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.akexorcist.googledirection.model.Step;

import java.util.ArrayList;

public class CustomRoute implements Parcelable
{
	private Position startPosition;
	private int sportType;
	private float distance = -1;
	private long duration = -1;
	private ArrayList<Place> places;
	private float rating = -1;
	private ArrayList<Step> steps;

	public Position getStartPosition()
	{
		return startPosition;
	}

	public void setStartPosition(Position position)
	{
		this.startPosition = position;
	}

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

	public long getDuration()
	{
		return this.duration;
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
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

	public CustomRoute(Position position, int sportType, ArrayList<Place> places)
	{
		this.startPosition = position;
		this.sportType = sportType;
		this.places = places;
	}

	public CustomRoute(Position position, int sportType, float distance, ArrayList<Place> places, float rating)
	{
		this(position, sportType, places);
		this.distance = distance;
		this.rating = rating;
	}

	protected CustomRoute(Parcel in)
	{
		startPosition = (Position) in.readValue(Position.class.getClassLoader());
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
		dest.writeValue(startPosition);
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