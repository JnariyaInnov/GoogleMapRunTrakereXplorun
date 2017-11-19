package brice.explorun.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import brice.explorun.R;
import brice.explorun.models.CustomRoute;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.TimeUtility;

public class RouteInfoFragment extends Fragment
{
	private ImageView sportTypeImage;
	private TextView sportTypeText;
	private TextView distanceText;
	private TextView durationText;
	private TextView arrivalTimeText;

	private int durationInMinutes = 0;

	private BroadcastReceiver tickReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			arrivalTimeText.setText(TimeUtility.computeEstimatedTimeOfArrival(durationInMinutes));
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_route_info,container,false);

		this.sportTypeImage = view.findViewById(R.id.sport_type_image);
		this.sportTypeText = view.findViewById(R.id.sport_type_text);
		this.distanceText = view.findViewById(R.id.distance_text);
		this.durationText = view.findViewById(R.id.duration_text);
		this.arrivalTimeText = view.findViewById(R.id.arrival_time_text);

		this.getActivity().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

		return view;
	}

	public void update(CustomRoute route)
	{
		this.updateSportImageAndText(route.getSportType());

		double distanceInKm = route.getDistance()/1000.0;
		this.distanceText.setText(LocationUtility.formatDistance(this.getActivity(), distanceInKm));

		float averageSpeed = SportUtility.getAverageSpeedFromSport(route.getSportType());
		this.durationInMinutes =  TimeUtility.convertTimeToMinutes(distanceInKm / averageSpeed);

		this.durationText.setText(TimeUtility.formatDuration(this.getActivity(), durationInMinutes));

		this.arrivalTimeText.setText(TimeUtility.computeEstimatedTimeOfArrival(durationInMinutes));
	}

	private void updateSportImageAndText(int sportType)
	{
		switch (sportType)
		{
			case SportUtility.TRAIL:
				this.sportTypeImage.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_run));
				this.sportTypeImage.setContentDescription(this.getResources().getString(R.string.form_trail_radio));
				this.sportTypeText.setText(R.string.form_trail_radio);
				break;

			case SportUtility.RUNNING:
				this.sportTypeImage.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_run));
				this.sportTypeImage.setContentDescription(this.getResources().getString(R.string.form_run_radio));
				this.sportTypeText.setText(R.string.form_run_radio);
				break;

			default:
				this.sportTypeImage.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_walk));
				this.sportTypeImage.setContentDescription(this.getResources().getString(R.string.form_walk_radio));
				this.sportTypeText.setText(R.string.form_walk_radio);
		}
	}

	@Override
	public void onDestroy()
	{
		if(this.tickReceiver != null)
		{
			this.getActivity().unregisterReceiver(this.tickReceiver);
		}
		super.onDestroy();
	}
}
