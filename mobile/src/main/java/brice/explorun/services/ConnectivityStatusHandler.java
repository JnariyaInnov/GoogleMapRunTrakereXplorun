package brice.explorun.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import brice.explorun.R;
import brice.explorun.activities.MainActivity;
import brice.explorun.fragments.MapFragment;
import brice.explorun.utilities.Utility;
import brice.explorun.fragments.NearbyAttractionsFragment;

public class ConnectivityStatusHandler extends BroadcastReceiver
{
	private MainActivity activity; // Parent activity

	public ConnectivityStatusHandler(MainActivity activity)
	{
		this.activity = activity;
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
				// Retrieving the current displayed fragment
				Fragment fragment = this.activity.getSupportFragmentManager().findFragmentById(R.id.container);
				// If the user is on the NearbyAttractionsFragment, we update the list
				if (fragment instanceof NearbyAttractionsFragment)
				{
					NearbyAttractionsFragment frag = (NearbyAttractionsFragment) fragment;
					frag.getNearbyPlaces();
				}
				else if (fragment instanceof MapFragment)
				{
					MapFragment frag = (MapFragment) fragment;
					frag.getNearbyPlaces();
				}
				noNetworkLabel.setVisibility(View.GONE);
			}
			else
			{
				noNetworkLabel.setVisibility(View.VISIBLE);
			}
		}
	}
}
