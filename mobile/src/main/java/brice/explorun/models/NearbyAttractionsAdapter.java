package brice.explorun.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import brice.explorun.R;

public class NearbyAttractionsAdapter extends ArrayAdapter<Place>
{
	public NearbyAttractionsAdapter(Context context, ArrayList<Place> places)
	{
		super(context, 0, places);
	}

	@Override
	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent)
	{
		ViewHolder holder;

		// Get the data item for this position
		Place place = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.nearby_attraction, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		// Populate the data into the template view using the data object
		if (place != null)
		{
			holder.nameView.setText(place.getName());

			double distance = place.getDistance();
			String distanceString;
			if (distance < 1)
			{
				// We round the distance to the nearest multiple of 10
				distance = Math.round(Math.round(distance*1000)/10.0)*10.0;
				distanceString = String.format(getContext().getResources().getString(R.string.distance_in_m), distance);
			}
			else
			{
				distance = Math.round(distance*10.0)/10.0;
				distanceString = String.format(getContext().getResources().getString(R.string.distance_in_km), distance);
			}
			holder.distanceView.setText(distanceString);

			// Add the photo of the place to the view
			Photo photo = place.getPhoto();
			if (photo == null || photo.getBitmap() == null) // If the place has no photo
			{
				holder.imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_photo_camera));
				holder.imageView.setContentDescription(getContext().getResources().getString(R.string.no_photo));
			}
			else
			{
				holder.imageView.setImageBitmap(photo.getBitmap());
				holder.imageView.setContentDescription(place.getPhoto().getAttribution());
			}
		}
		// Return the completed view to render on screen
		return convertView;
	}

	/**
	 * Cache of the children views for a forecast list item.
	 */
	private static class ViewHolder
	{
		private final ImageView imageView;
		private final TextView nameView;
		private final TextView distanceView;

		private ViewHolder(View view)
		{
			this.imageView = view.findViewById(R.id.attraction_photo);
			this.nameView = view.findViewById(R.id.attraction_name);
			this.distanceView = view.findViewById(R.id.attraction_distance);
		}
	}
}
