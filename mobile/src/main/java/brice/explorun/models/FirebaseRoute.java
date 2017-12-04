package brice.explorun.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

import brice.explorun.utilities.SportUtility;

public class FirebaseRoute implements Parcelable
{
	private String id = "";
	private Date date;
	private int sportType = SportUtility.WALKING;
	private double distance = 0;
	private long duration = 0;
	private Position startPosition;
	private ArrayList<FirebasePlace> places;

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

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

	public Position getStartPosition()
	{
		return startPosition;
	}

	public void setStartPosition(Position startPosition)
	{
		this.startPosition = startPosition;
	}

	public ArrayList<FirebasePlace> getPlaces()
	{
		return places;
	}

	public void setPlaces(ArrayList<FirebasePlace> places)
	{
		this.places = places;
	}

	public FirebaseRoute(Date date, int sportType, double distance, long duration, Position startPosition, ArrayList<FirebasePlace> places)
	{
		this.date = date;
		this.sportType = sportType;
		this.distance = distance;
		this.duration = duration;
		this.startPosition = startPosition;
		this.places = places;
	}

	// Constructor required by Firestore
	public FirebaseRoute() {}

	protected FirebaseRoute(Parcel in)
	{
		long tmpDate = in.readLong();
		date = tmpDate != -1 ? new Date(tmpDate) : null;
		sportType = in.readInt();
		distance = in.readDouble();
		duration = in.readLong();
		startPosition = (Position) in.readValue(Position.class.getClassLoader());
		if (in.readByte() == 0x01)
		{
			places = new ArrayList<>();
			in.readList(places, FirebasePlace.class.getClassLoader());
		}
		else
		{
			places = null;
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
		dest.writeLong(date != null ? date.getTime() : -1L);
		dest.writeInt(sportType);
		dest.writeDouble(distance);
		dest.writeLong(duration);
		dest.writeValue(startPosition);
		if (places == null)
		{
			dest.writeByte((byte) (0x00));
		}
		else
		{
			dest.writeByte((byte) (0x01));
			dest.writeList(places);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<FirebaseRoute> CREATOR = new Parcelable.Creator<FirebaseRoute>()
	{
		@Override
		public FirebaseRoute createFromParcel(Parcel in)
		{
			return new FirebaseRoute(in);
		}

		@Override
		public FirebaseRoute[] newArray(int size)
		{
			return new FirebaseRoute[size];
		}
	};
}