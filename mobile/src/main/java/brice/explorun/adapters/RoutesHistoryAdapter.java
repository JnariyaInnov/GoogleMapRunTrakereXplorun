package brice.explorun.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.adapters.ArraySwipeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import brice.explorun.R;
import brice.explorun.activities.MainActivity;
import brice.explorun.models.FirebaseRoute;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.TimeUtility;
import brice.explorun.utilities.Utility;

public class RoutesHistoryAdapter extends ArraySwipeAdapter<FirebaseRoute>
{
	private Context context;

	public RoutesHistoryAdapter(Context context, ArrayList<FirebaseRoute> routes)
	{
		super(context, 0, routes);
		this.context = context;
	}

	@Override
	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent)
	{
		ViewHolder holder;

		// Get the data item for this position
		final FirebaseRoute route = (FirebaseRoute)getItem(position);
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
			holder.itemLayout.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					viewRouteOnMap(route);
				}
			});
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
			holder.deleteButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					deleteRoute(route);
				}
			});
		}

		// Return the completed view to render on screen
		return convertView;
	}

	public int getSwipeLayoutResourceId(int position)
	{
		return R.id.swipe_layout;
	}

	private void viewRouteOnMap(FirebaseRoute route)
	{
		// To view the route on the map, the user needs an internet connection because of the call to Google Directions API
		if (Utility.isOnline(this.context))
		{
			Bundle args = new Bundle();
			args.putParcelable("route", route);
			// Setting the new fragment in MainActivity
			MainActivity activity = (MainActivity) this.context;
			activity.getSupportActionBar().setTitle(activity.getResources().getString(R.string.app_name));
			activity.selectItem(activity.getNavigationView().getMenu().getItem(0), args);
		}
		else
		{
			Toast.makeText(this.context, R.string.view_route_on_map_error, Toast.LENGTH_SHORT).show();
		}
	}

	private void deleteRoute(FirebaseRoute route)
	{
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null)
		{
			db.collection("users").document(user.getUid()).collection("routes").document(route.getId()).delete();
			this.remove(route);
			notifyDataSetChanged();
		}
	}

	/**
	 * Cache of the children views for a place list item.
	 */
	private static class ViewHolder
	{
		private final LinearLayout itemLayout;
		private final ImageView imageView;
		private final TextView distanceView;
		private final TextView durationView;
		private final TextView sportTypeView;
		private final TextView dateView;
		private final Button deleteButton;

		private ViewHolder(View view)
		{
			this.itemLayout = view.findViewById(R.id.item_layout);
			this.imageView = view.findViewById(R.id.sport_type_image);
			this.durationView = view.findViewById(R.id.duration);
			this.distanceView = view.findViewById(R.id.distance);
			this.sportTypeView = view.findViewById(R.id.sport_type_text);
			this.dateView = view.findViewById(R.id.date);
			this.deleteButton = view.findViewById(R.id.btn_delete);
		}
	}
}
