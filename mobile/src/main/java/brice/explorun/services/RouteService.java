package brice.explorun.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.akexorcist.googledirection.model.Step;

import java.util.List;

import brice.explorun.models.Place;
import brice.explorun.utilities.LocationUtility;

public class RouteService extends Service
{
	private final String TAG = "eX_RouteService";

	public static boolean isStarted = false;
	private List<Step> steps;
	private List<Place> places;

	private int placeIndex = 0;
	private int instructionIndex = 0;
	Intent ttsBroadcastIntent = new Intent("ex_tts");
	Intent updateDistanceBroadcastIntent = new Intent("ex_distance");

	private double distance = 0;
	private double lastSentDistance = 0;
	private float latitude = -1;
	private float longitude = -1;

	private BroadcastReceiver locReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			update();
		}
	};

	@Override
	public void onCreate() {
		isStarted = true;
		IntentFilter filter = new IntentFilter("ex_location");
		this.registerReceiver(this.locReceiver, filter);
		Log.i(this.TAG, "Service successfully started");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		this.steps = intent.getParcelableArrayListExtra("steps");
		this.places = intent.getParcelableArrayListExtra("places");
		update();
		return super.onStartCommand(intent, flags, startId);
	}

	public void update() {
		Log.i(this.TAG, "Update service data");
		// Get location in preferences
		float[] loc = LocationUtility.getLocationFromPreferences(this);
		//Enunciate current instruction if distance is less than 100m
		if(this.steps != null && this.instructionIndex < this.steps.size()) {
			Step curStep = this.steps.get(this.instructionIndex);
			if (LocationUtility.distanceBetweenCoordinates(loc[0], loc[1], curStep.getStartLocation().getLatitude(), curStep.getStartLocation().getLongitude()) < 0.1) {
				sendBroadcast(this.ttsBroadcastIntent.putExtra("text", this.steps.get(this.instructionIndex).getHtmlInstruction()));
				this.instructionIndex++;
			}
		}

		if(this.places != null && this.placeIndex < this.places.size()) {
			Place curStep = this.places.get(this.placeIndex);
			if (LocationUtility.distanceBetweenCoordinates(loc[0], loc[1], curStep.getLatitude(), curStep.getLongitude()) < 0.1) {
				Log.i("tts name place",places.get(this.placeIndex).getDescription());
				sendBroadcast(this.ttsBroadcastIntent.putExtra("text", this.places.get(this.placeIndex).getDescription()));
				this.placeIndex++;
			}
		}

		// Update total distance
		if (this.latitude == -1f && this.longitude == -1f)
		{
			this.latitude = loc[0];
			this.longitude = loc[1];
		}
		else
		{
			this.distance += LocationUtility.distanceBetweenCoordinates(loc[0], loc[1], this.latitude, this.longitude);
			this.latitude = loc[0];
			this.longitude = loc[1];
			double delta = 0.1;
			if (this.distance < 1)
			{
				delta = 0.05;
			}
			if (this.distance - this.lastSentDistance > delta)
			{
				sendBroadcast(this.updateDistanceBroadcastIntent.putExtra("distance", this.distance));
				this.lastSentDistance = this.distance;
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i("explorun_route","Stopping Route service");
		isStarted = false;
		this.steps = null;
		this.instructionIndex = 0;
		this.unregisterReceiver(this.locReceiver);
		super.onDestroy();
	}
}
