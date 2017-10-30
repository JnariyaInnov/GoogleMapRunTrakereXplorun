package brice.explorun;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class NetworkHandler extends Handler
{
	public NetworkHandler(final Activity activity, final TextView noNetworkLabel, final int checkInterval)
	{
		this.post(new Runnable()
		{
			@Override
			public void run()
			{
				//call function
				if(Utility.isOnline(activity.getBaseContext())) {
					noNetworkLabel.setVisibility(View.GONE);
				}
				else {
					noNetworkLabel.setVisibility(View.VISIBLE);
				}
				postDelayed(this, checkInterval);
			}
		});
	}
}
