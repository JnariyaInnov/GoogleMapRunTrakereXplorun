package brice.explorun.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.model.Step;

import java.util.ArrayList;

import brice.explorun.R;
import brice.explorun.controllers.WikiAttractionController;
import brice.explorun.models.CustomRoute;
import brice.explorun.models.Place;
import brice.explorun.models.RouteObserver;
import brice.explorun.services.RouteService;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.TimeUtility;
import brice.explorun.utilities.Utility;


public class RouteInfoFragment extends Fragment
{
	private ImageView sportTypeImage;
	private TextView sportTypeText;
	private TextView distanceText;
	private TextView durationText;
	private TextView arrivalTimeText;

	private int durationInMinutes = 0;
	private ArrayList<Step> steps = new ArrayList<>();
	private ArrayList<Place> places = new ArrayList<>();

	private RouteObserver observer;

	protected WikiAttractionController wikiAttractionController;

	private BroadcastReceiver tickReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			arrivalTimeText.setText(TimeUtility.computeEstimatedTimeOfArrival(getActivity(), durationInMinutes));
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
		Button startRouteButton = view.findViewById(R.id.btn_start_route);
		startRouteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startRoute();
			}
		});
		Button cancelRouteButton = view.findViewById(R.id.btn_cancel_route);
		cancelRouteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				cancelRoute();
			}
		});

		this.getActivity().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

		try
		{
			this.observer = (RouteObserver) getParentFragment();
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(getParentFragment().toString() + " must implement RouteObserver");
		}

		this.wikiAttractionController = new WikiAttractionController(this);

		return view;
	}

	public void update(CustomRoute route)
	{
		if (route != null)
		{
			this.updateSportImageAndText(route.getSportType());

			float distance = route.getDistance();
			this.distanceText.setText(LocationUtility.formatDistance(this.getActivity(), distance));

			// If the route has a duration, it means that this is a route stored in the user's history
			if (route.getDuration() != -1)
			{
				long duration = route.getDuration();
				this.durationText.setText(TimeUtility.formatDurationHms(this.getActivity(), duration));
				this.durationInMinutes = (int)Math.round(duration / 1000.0 / 60.0);
				this.sportTypeText.setText(String.format(getResources().getString(R.string.average_speed), TimeUtility.computeAverageSpeed(distance, duration)));
			}
			else // Basic case: the user has searched for a route
			{
				float averageSpeed = SportUtility.getAverageSpeedFromSport(this.getActivity(), route.getSportType());
				this.durationInMinutes = TimeUtility.convertTimeToMinutes(distance/1000.0 / averageSpeed);
				this.durationText.setText(TimeUtility.formatDuration(this.getActivity(), durationInMinutes));
			}

			this.arrivalTimeText.setText(TimeUtility.computeEstimatedTimeOfArrival(this.getActivity(), this.durationInMinutes));

			this.steps = route.getSteps();

			this.places = route.getPlaces();
			this.wikiAttractionController.setPlaces(this.places);
		}
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

	public void startRoute()
	{
		if (Utility.isOnline(this.getActivity()))
		{
			if (this.observer != null)
			{
				this.observer.onWikiSearch();
			}
			this.wikiAttractionController.getWikiAttractions();
		}
		else
		{
			Toast.makeText(this.getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
		}
	}

	public void cancelRoute()
	{
		if (this.observer != null)
		{
			this.observer.onRouteStop();
		}
	}

	public void onWikiResponse()
	{
		Intent routeServiceIntent = new Intent(getActivity(), RouteService.class);
		routeServiceIntent.putParcelableArrayListExtra("steps", steps);
		routeServiceIntent.putParcelableArrayListExtra("places", places);

		if (!RouteService.isStarted)
		{
			getActivity().startService(routeServiceIntent);
		}
		if (this.observer != null)
		{
			this.observer.onRouteStart();
		}
	}
}
