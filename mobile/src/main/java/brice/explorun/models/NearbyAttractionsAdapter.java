package brice.explorun.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
		// Get the data item for this position
		Place place = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.nearby_attraction, parent, false);
		}
		// Lookup view for data population
		TextView name = convertView.findViewById(R.id.attraction_name);
		TextView txtDistance = convertView.findViewById(R.id.attraction_distance);
		// Populate the data into the template view using the data object
		if (place != null)
		{
			name.setText(place.getName());

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
			txtDistance.setText(distanceString);
		}
		// Return the completed view to render on screen
		return convertView;
	}
}
