package brice.explorun.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.TextView;

import brice.explorun.R;
import brice.explorun.activities.MainActivity;
import brice.explorun.fragments.PlacesObserverFragment;
import brice.explorun.utilities.Utility;

public class ConnectivityStatusHandler extends BroadcastReceiver
{
	private MainActivity activity; // Parent activity
	private boolean isConnected;

	public ConnectivityStatusHandler(MainActivity activity)
	{
		this.activity = activity;
		this.isConnected = Utility.isOnline(activity);
	}

	public void onReceive(Context context, Intent intent)
	{
		if(intent == null || intent.getExtras() == null)
			return;

		updateNoNetworkLabel();
	}

	public void updateNoNetworkLabel()
	{
		// Retrieving label to show if the user has no internet connection
		TextView noNetworkLabel = this.activity.findViewById(R.id.no_network_label);

		// The label can be null if we are on an offline fragment
		if (noNetworkLabel != null)
		{
			//call function
			if (Utility.isOnline(this.activity.getBaseContext()))
			{
				if (!isConnected)
				{
					// Retrieving the current displayed fragment
					Fragment fragment = this.activity.getSupportFragmentManager().findFragmentById(R.id.container);
					// If the user is on a PlacesObserverFragment
					if (fragment instanceof PlacesObserverFragment)
					{
						PlacesObserverFragment frag = (PlacesObserverFragment) fragment;
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
	}
}
