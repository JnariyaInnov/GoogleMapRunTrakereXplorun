package brice.explorun.services;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import brice.explorun.models.Photo;
import brice.explorun.controllers.NearbyAttractionsController;

public class PhotoRetriever extends AsyncTask<Photo, Void, Photo>
{
	private NearbyAttractionsController manager;
	private PlacesClient placesClient;

	public PhotoRetriever(NearbyAttractionsController manager, PlacesClient placesClient)
	{
		this.manager = manager;
		this.placesClient = placesClient;
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

		final Photo photo = params[0];

		// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
		List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);

		// Get a Place object
		FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(photo.getPlaceId(), fields).build();

		this.placesClient.fetchPlace(placeRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>()
		{
			@Override
			public void onSuccess(FetchPlaceResponse fetchPlaceResponse)
			{
				Place place = fetchPlaceResponse.getPlace();

				// Get the photo metadata.
				PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

				// Get the attribution text.
				final String attribution = photoMetadata.getAttributions();

				// Create a FetchPhotoRequest.
				FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
						.setMaxWidth(500) // Optional.
						.setMaxHeight(300) // Optional.
						.build();

				placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>()
				{
					@Override
					public void onSuccess(FetchPhotoResponse fetchPhotoResponse)
					{
						Bitmap bitmap = fetchPhotoResponse.getBitmap();
						photo.setBitmap(bitmap);
						photo.setAttribution(attribution);
						onPostExecute(photo);
					}
				}).addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						if (e instanceof ApiException) {
							ApiException apiException = (ApiException) e;
						}
						Log.e("fetchPhoto", e.getMessage());
					}
				});
			}
		}).addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Log.e("fetchPlace", e.getMessage());
			}
		});

		return photo;
	}

	@Override
	protected void onPostExecute(Photo photo)
	{
		this.manager.updatePlacePhoto(photo);
	}
}
