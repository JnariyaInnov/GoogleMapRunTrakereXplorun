package brice.explorun.services;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import brice.explorun.models.Photo;
import brice.explorun.controllers.NearbyAttractionsController;

public class PhotoRetriever extends AsyncTask<Photo, Void, Photo>
{
	private NearbyAttractionsController manager;

	public PhotoRetriever(NearbyAttractionsController manager)
	{
		this.manager = manager;
	}

	/**
	 * Loads the first photo for a place id from the Geo Data API.
	 * The photo  must be the first (and only) parameter.
	 */
	@Override
	protected Photo doInBackground(Photo... params)
	{
		if (params.length < 1)
		{
			return null;
		}

		Photo photo = params[0];
		PlacePhotoMetadataResult result = Places.GeoDataApi.getPlacePhotos(LocationService.mGoogleApiClient, photo.getPlaceId()).await();

		if (result.getStatus().isSuccess())
		{
			PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
			if (photoMetadataBuffer.getCount() > 0 && !isCancelled())
			{
				// Get the first bitmap and its attributions.
				PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
				if (photoMetadata != null)
				{
					String attribution = photoMetadata.getAttributions().toString();
					// Load a bitmap for this photo.
					Bitmap image = photoMetadata.getPhoto(LocationService.mGoogleApiClient).await().getBitmap();

					photo.setAttribution(attribution);
					photo.setBitmap(image);
				}
			}
			// Release the PlacePhotoMetadataBuffer.
			photoMetadataBuffer.release();
		}
		return photo;
	}

	@Override
	protected void onPostExecute(Photo photo)
	{
		this.manager.updatePlacePhoto(photo);
	}
}
