package brice.explorun.models;

/**
 * Class which represents a photo of a place
 */

public class Photo
{
	private int width = 0; // Width of the photo
	private int height = 0; // Height of the photo

	private String reference = ""; // Reference of the photo, used by Google
	private String url = ""; // Url of the photo

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

	public String getReference()
	{
		return reference;
	}

	public void setReference(String reference)
	{
		this.reference = reference;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public Photo(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.reference = "";
		this.url = "";
	}

	public Photo(int width, int height, String reference)
	{
		this(width, height);
		this.reference = reference;
	}
}
