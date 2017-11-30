package brice.explorun.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import brice.explorun.R;
import brice.explorun.models.CustomRoute;
import brice.explorun.models.FirebaseRoute;
import brice.explorun.models.Place;
import brice.explorun.models.RouteObserver;
import brice.explorun.services.RouteService;
import brice.explorun.utilities.LocationUtility;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.TimeUtility;

public class CurrentRouteFragment extends Fragment
{
	private final String BASE_KEY = "base";
	private final String LAST_STOP_TIME_KEY = "last_stop_time";
	private final String DISTANCE_KEY = "distance";
	private final String RUNNING_KEY = "is_running";

	private ScrollView layout;
	private TextView distanceText;
	private TextView speedText;
	private ImageView speedImage;
	private Chronometer chronometer;
	private Button pauseButton;

	private Animation animation;

	private CustomRoute customRoute;
	private long base = 0;
	private double distance = 0;
	private long lastStopTime = 0;
	private long duration = 0;
	private boolean isRunning = true;

	private RouteObserver observer;

	private BroadcastReceiver distanceReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			distance = intent.getDoubleExtra("distance", 0);
			distanceText.setText(LocationUtility.formatDistance(getActivity(), distance));
			speedText.setText(String.format(getResources().getString(R.string.average_speed), TimeUtility.computeAverageSpeed(distance, SystemClock.elapsedRealtime()-base)));
		}
	};

	private View.OnClickListener pauseListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			pauseRoute();
		}
	};

	private View.OnClickListener resumeListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			resumeRoute();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_current_route,container,false);

		this.layout = view.findViewById(R.id.current_route_fragment);
		this.distanceText = view.findViewById(R.id.distance_text);
		this.speedText = view.findViewById(R.id.average_speed);
		this.speedImage = view.findViewById(R.id.average_speed_image);
		this.chronometer = view.findViewById(R.id.duration);
		this.pauseButton = view.findViewById(R.id.btn_pause_route);
		this.pauseButton.setOnClickListener(this.pauseListener);
		Button stopButton = view.findViewById(R.id.btn_stop_route);
		stopButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				stopRoute();
			}
		});

		this.animation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_down);

		this.getActivity().registerReceiver(distanceReceiver, new IntentFilter("ex_distance"));

		if (savedInstanceState != null)
		{
			this.base = savedInstanceState.getLong(BASE_KEY);
			this.lastStopTime = savedInstanceState.getLong(LAST_STOP_TIME_KEY);
			this.distance = savedInstanceState.getDouble(DISTANCE_KEY);
			this.isRunning = savedInstanceState.getBoolean(RUNNING_KEY);
		}

		try
		{
			this.observer = (RouteObserver) getParentFragment();
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(getParentFragment().toString() + " must implement RouteObserver");
		}

		return view;
	}

	public void update(CustomRoute customRoute)
	{
		if (customRoute.getSportType() == SportUtility.WALKING)
		{
			this.speedImage.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_walk));
		}
		else
		{
			this.speedImage.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_run));
		}
		this.distanceText.setText(LocationUtility.formatDistance(getActivity(), distance));
		if (this.base == 0)
		{
			this.base = SystemClock.elapsedRealtime();
			this.chronometer.setBase(this.base);
		}
		else
		{
			if (this.isRunning)
			{
				this.chronometer.setBase(this.base);
			}
			else
			{
				this.chronometer.setBase(this.base + SystemClock.elapsedRealtime() - this.lastStopTime);
			}
		}
		if (this.isRunning)
		{
			this.chronometer.start();
		}
		else
		{
			this.updateLayoutOnPause();
		}
		this.speedText.setText(String.format(getResources().getString(R.string.average_speed), 0));
		this.chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
		{
			@Override
			public void onChronometerTick(Chronometer chronometer)
			{
				speedText.setText(String.format(getResources().getString(R.string.average_speed), TimeUtility.computeAverageSpeed(distance, SystemClock.elapsedRealtime()-chronometer.getBase())));
			}
		});
		this.customRoute = customRoute;
	}

	public void onSaveInstanceState(Bundle outBundle)
	{
		outBundle.putLong(BASE_KEY, this.base);
		outBundle.putLong(LAST_STOP_TIME_KEY, this.lastStopTime);
		outBundle.putDouble(DISTANCE_KEY, this.distance);
		outBundle.putBoolean(RUNNING_KEY, this.isRunning);
		super.onSaveInstanceState(outBundle);
	}

	public void onDestroy()
	{
		if (this.distanceReceiver != null)
		{
			this.getActivity().unregisterReceiver(this.distanceReceiver);
		}
		super.onDestroy();
	}

	public void pauseRoute()
	{
		this.chronometer.stop();
		this.lastStopTime = SystemClock.elapsedRealtime();
		this.updateLayoutOnPause();
		this.isRunning = false;
	}

	public void updateLayoutOnPause()
	{
		this.pauseButton.setText(R.string.resume_route_text);
		this.pauseButton.setBackgroundColor(this.getResources().getColor(android.R.color.holo_green_light));
		this.pauseButton.setOnClickListener(this.resumeListener);
	}

	public void resumeRoute()
	{
		this.base = this.base + SystemClock.elapsedRealtime() - this.lastStopTime;
		this.chronometer.setBase(this.base);
		this.chronometer.start();
		this.pauseButton.setText(R.string.pause_route_text);
		this.pauseButton.setBackgroundColor(this.getResources().getColor(android.R.color.holo_orange_light));
		this.pauseButton.setOnClickListener(this.pauseListener);
		this.isRunning = true;
	}

	public void stopRoute()
	{
		this.chronometer.stop();
		this.duration = SystemClock.elapsedRealtime() - this.base;
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setMessage(R.string.route_saved).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				dialogInterface.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		addRouteInFirebase();
		Intent intent = new Intent(this.getActivity(), RouteService.class);
		this.getActivity().stopService(intent);
		this.layout.setAnimation(this.animation);
		this.layout.setVisibility(View.GONE);
		this.layout.startAnimation(this.animation);
		if (this.observer != null)
		{
			this.observer.onRouteStop();
		}
	}

	public void addRouteInFirebase()
	{
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null)
		{
			float[] loc = LocationUtility.getLocationFromPreferences(this.getActivity());
			LatLng pos = new LatLng(loc[0], loc[1]);
			ArrayList<LatLng> places = new ArrayList<>();
			for (Place p: this.customRoute.getPlaces())
			{
				places.add(new LatLng(p.getLatitude(), p.getLongitude()));
			}
			FirebaseRoute route = new FirebaseRoute(new Date(), this.customRoute.getSportType(), this.distance, this.duration, pos, places);
			db.collection("users").document(user.getUid()).collection("routes").add(route).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
			{
				@Override
				public void onSuccess(DocumentReference documentReference)
				{
					Log.d("CurrentRoute", "Route successfully stored");
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					Log.e("CurrentRoute", "Error while storing route:" + e.getMessage());
				}
			});
		}
		this.distance = 0;
		this.base = 0;
		this.lastStopTime = 0;
	}
}
