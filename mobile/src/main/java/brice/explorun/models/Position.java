package brice.explorun.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

@Keep
public class Position implements Parcelable
{
	private double latitude;
	private double longitude;

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	// Constructor required by Firestore
	public Position(){}

	public Position(double latitude, double longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
	}

	protected Position(Parcel in)
	{
		latitude = in.readDouble();
		longitude = in.readDouble();
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Position> CREATOR = new Parcelable.Creator<Position>()
	{
		@Override
		public Position createFromParcel(Parcel in) {
			return new Position(in);
		}

		@Override
		public Position[] newArray(int size) {
			return new Position[size];
		}
	};
}