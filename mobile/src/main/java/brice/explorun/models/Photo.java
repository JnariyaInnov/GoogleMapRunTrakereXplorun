package brice.explorun.models;

import android.graphics.Bitmap;

/**
 * Class which represents a photo of a place
 */

public class Photo
{
	private String placeId = ""; // Id of the place to which the photo belongs

	private int width = 0; // Width of the photo
	private int height = 0; // Height of the photo

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

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
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

	public Photo(String placeId, int width, int height)
	{
		this.setPlaceId(placeId);
		this.setWidth(width);
		this.setHeight(height);
	}

	public Photo(String placeId, int width, int height, String attribution, Bitmap bitmap)
	{
		this(placeId, width, height);
		this.setAttribution(attribution);
		this.setBitmap(bitmap);
	}
}
