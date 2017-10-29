package brice.explorun;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{
	private Fragment mapsFragment;
	private AlertDialog dialog;
	private final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Utility.isOnline(this))
        {
			this.dialog = Utility.showAlertDialog(R.string.network_error, R.string.no_network, this);
		}

		if (savedInstanceState != null)
		{
			//Restore the fragment's instance
			this.mapsFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mapFragment");
		}
		else
		{
			this.mapsFragment = new MapsFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, this.mapsFragment).commit();
		}
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_CHECK_SETTINGS:
				switch (resultCode)
				{
					case Activity.RESULT_OK:
						// Access to location granted by the user
						MapsFragment fragment = (MapsFragment) this.mapsFragment;
						fragment.getLocationManager().getLocation();
						break;

					case Activity.RESULT_CANCELED:
						// The user was asked to change settings, but chose not to
						break;

					default:
						break;
				}
				break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		//Save the fragment's instance
		getSupportFragmentManager().putFragment(outState, "mapFragment", this.mapsFragment);
	}

	protected void onDestroy()
	{
		//Dismiss the dialog to prevent error when destroying activity
    	if (this.dialog != null && this.dialog.isShowing())
    	{
    		this.dialog.dismiss();
		}
		super.onDestroy();
	}
}
