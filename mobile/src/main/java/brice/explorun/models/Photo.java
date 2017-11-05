package brice.explorun.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class which represents a photo of a place
 */

public class Photo implements Parcelable
{
	private String placeId = ""; // Id of the place to which the photo belongs
	private String attribution = ""; // Description of the photo (unable to load photo or to help disabled people)
	private Bitmap bitmap = null; // Bitmap representing the photo

	public String getPlaceId()
	{
		return placeId;
	}

	public void setPlaceId(String placeId)
	{
		this.placeId = placeId;
	}

	public String getAttribution()
	{
		return attribution;
	}

	public void setAttribution(String attribution)
	{
		this.attribution = attribution;
	}

	public Bitmap getBitmap()
	{
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}

	public Photo(String placeId)
	{
		this.setPlaceId(placeId);
		this.setAttribution("");
		this.setBitmap(null);
	}

	protected Photo(Parcel in)
	{
		this.placeId = in.readString();
		this.attribution = in.readString();
		this.bitmap = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
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
		dest.writeString(this.attribution);
		dest.writeValue(this.bitmap);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>()
	{
		@Override
		public Photo createFromParcel(Parcel in)
		{
			return new Photo(in);
		}

		@Override
		public Photo[] newArray(int size)
		{
			return new Photo[size];
		}
	};
}
