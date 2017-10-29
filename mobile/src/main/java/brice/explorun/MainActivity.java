package brice.explorun;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
			updateNoNetworkLabel(true);
			final Handler ha=new Handler();
			final MainActivity _this = this;
			ha.postDelayed(new Runnable() {

				@Override
				public void run() {
					//call function
					if(Utility.isOnline(getApplicationContext())){
						//TODO: Faire ca mieux (c'est mal mais j'ai faim dsl... )
						_this.mapsFragment = new MapsFragment();
						getSupportFragmentManager().beginTransaction().replace(R.id.container, _this.mapsFragment).commit();
					}
					else {
						ha.postDelayed(this, 10000);
					}
				}
			}, 10000);
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

	public void updateNoNetworkLabel(boolean visible){
		TextView noNetworkLabel = this.findViewById(R.id.no_network_label);
		if (visible){
			ViewGroup.LayoutParams params = noNetworkLabel.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			params.width = ViewGroup.LayoutParams.MATCH_PARENT;
			noNetworkLabel.setLayoutParams(params);
			noNetworkLabel.setVisibility(View.VISIBLE);
		}
		else{
			ViewGroup.LayoutParams params = noNetworkLabel.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = 0;
			params.width = 0;
			noNetworkLabel.setLayoutParams(params);
			noNetworkLabel.setVisibility(View.INVISIBLE);
		}
	}
}
