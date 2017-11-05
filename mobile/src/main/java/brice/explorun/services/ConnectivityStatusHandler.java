package brice.explorun.services;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import brice.explorun.R;
import brice.explorun.models.Utility;
import brice.explorun.fragments.NearbyAttractionsFragment;

public class ConnectivityStatusHandler extends Handler
{
	private static ConnectivityStatusHandler instance = null;

	private final int CHECK_INTERVAL = 5000; // Interval between two checks of the user's connection (in ms)
	private Runnable runnable; // Thread checking the internet connection

	private boolean isConnected; // True if the user has an internet connection, else false

	public static ConnectivityStatusHandler getInstance(AppCompatActivity activity)
	{
		if (instance == null)
		{
			instance = new ConnectivityStatusHandler(activity);
		}
		return instance;
	}

	private ConnectivityStatusHandler(final AppCompatActivity activity)
	{
		this.runnable = new Runnable()
		{
			@Override
			public void run()
			{
				// Retrieving label to show if the user has no internet connection
				TextView noNetworkLabel = activity.findViewById(R.id.no_network_label);

				// The label can be null if we are on an offline fragment
				if (noNetworkLabel != null)
				{
					//call function
					if (Utility.isOnline(activity.getBaseContext()))
					{
						// If the user's internet connection is back, we can resend requests
						if (!isConnected)
						{
							// Retrieving the current displayed fragment
							Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.container);
							// If the user is on the NearbyAttractionsFragment, we update the list
							if (fragment instanceof NearbyAttractionsFragment)
							{
								NearbyAttractionsFragment frag = (NearbyAttractionsFragment) fragment;
								frag.getNearbyPlaces();
							}
						}
						isConnected = true;
						noNetworkLabel.setVisibility(View.GONE);
					}
					else
					{
						isConnected = false;
						noNetworkLabel.setVisibility(View.VISIBLE);
					}
				}
				postDelayed(this, CHECK_INTERVAL);
			}
		};

		this.isConnected = Utility.isOnline(activity.getApplicationContext());
	}

	public void run()
	{
		this.post(this.runnable);
	}

	public void stopThread()
	{
		this.removeCallbacks(this.runnable);
		instance = null; // Manage change of orientation: a new activity is created
	}
}
