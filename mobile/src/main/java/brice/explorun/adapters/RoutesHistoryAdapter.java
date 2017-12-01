package brice.explorun.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import brice.explorun.R;
import brice.explorun.models.FirebaseRoute;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.TimeUtility;

public class RoutesHistoryAdapter extends ArrayAdapter<FirebaseRoute>
{
	public RoutesHistoryAdapter(Context context, ArrayList<FirebaseRoute> routes)
	{
		super(context, 0, routes);
	}

	@Override
	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent)
	{
		ViewHolder holder;

		// Get the data item for this position
		FirebaseRoute route = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		if (route != null)
		{
			switch (route.getSportType())
			{
				case SportUtility.RUNNING:
					holder.imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_run));
					holder.sportTypeView.setText(R.string.form_run_radio);
					break;

				case SportUtility.TRAIL:
					holder.imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_run));
					holder.sportTypeView.setText(R.string.form_trail_radio);
					break;

				default:
					holder.imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_walk));
					holder.sportTypeView.setText(R.string.form_walk_radio);
					break;
			}
			holder.distanceView.setText(LocationUtility.formatDistance(getContext(), route.getDistance()));
			holder.durationView.setText(TimeUtility.formatDurationHms(getContext(), route.getDuration()));
			holder.dateView.setText(TimeUtility.formatDate(getContext(), route.getDate()));
		}

		// Return the completed view to render on screen
		return convertView;
	}

	/**
	 * Cache of the children views for a place list item.
	 */
	private static class ViewHolder
	{
		private final ImageView imageView;
		private final TextView distanceView;
		private final TextView durationView;
		private final TextView sportTypeView;
		private final TextView dateView;

		private ViewHolder(View view)
		{
			this.imageView = view.findViewById(R.id.sport_type_image);
			this.durationView = view.findViewById(R.id.duration);
			this.distanceView = view.findViewById(R.id.distance);
			this.sportTypeView = view.findViewById(R.id.sport_type_text);
			this.dateView = view.findViewById(R.id.date);
		}
	}
}
