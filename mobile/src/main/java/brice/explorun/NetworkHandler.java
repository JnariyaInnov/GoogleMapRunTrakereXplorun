package brice.explorun;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class NetworkHandler extends Handler
{
	private TextView noNetworkLabel;

	public NetworkHandler(final Activity activity, TextView noNetworkLabel)
	{
		this.noNetworkLabel = noNetworkLabel;
		this.post(new Runnable()
		{
			@Override
			public void run()
			{
				//call function
				if(Utility.isOnline(activity.getApplicationContext()))
				{
					updateNoNetworkLabel(false);
				}
				else
				{
					updateNoNetworkLabel(true);
				}
				postDelayed(this, 5000);
			}
		});
	}

	private void updateNoNetworkLabel(boolean visible)
	{
		if (visible)
		{
			this.noNetworkLabel.setVisibility(View.VISIBLE);
		}
		else
		{
			this.noNetworkLabel.setVisibility(View.GONE);
		}
	}
}
