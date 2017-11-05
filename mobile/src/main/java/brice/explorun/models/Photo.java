package brice.explorun.models;

import android.graphics.Bitmap;

/**
 * Class which represents a photo of a place
 */

public class Photo
{
	private String placeId = ""; // Id of the place to which the photo belongs
	private String attribution = ""; // Attribution of the photo, used by Google
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
}
