package brice.explorun.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;

import brice.explorun.R;
import brice.explorun.fragments.AboutFragment;
import brice.explorun.fragments.MapFragment;
import brice.explorun.fragments.FormFragment;
import brice.explorun.fragments.NearbyAttractionsFragment;
import brice.explorun.services.ConnectivityStatusHandler;
import brice.explorun.services.LocationService;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private Fragment fragment;
	private final int REQUEST_CHECK_SETTINGS = 0x1;
	private final int MY_PERMISSIONS_REQUEST_GPS = 0;

	public GoogleApiClient mGoogleApiClient = null;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private NavigationView navigationView;

	private String mTitle;
	private int selectedItemId;

	private ConnectivityStatusHandler connectivityStatusHandler;

	public NavigationView getNavigationView() { return this.navigationView; }

	public ConnectivityStatusHandler getConnectivityStatusHandler() { return this.connectivityStatusHandler; }

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        Log.d("eX_lifeCycle", "main => onCreate()");
        setContentView(R.layout.activity_main);

		// Create an instance of GoogleAPIClient.
		if (this.mGoogleApiClient == null)
		{
			this.mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.addApi(Places.GEO_DATA_API)
					.build();
		}

		this.mDrawerLayout = findViewById(R.id.drawer_layout);
		this.navigationView = findViewById(R.id.navigation);

		//setting up selected item listener
		this.navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						selectItem(menuItem, null);
						return true;
					}
				});

		this.mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
		{
			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view)
			{
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(getResources().getString(R.string.drawer_title));
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.addDrawerListener(this.mDrawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState != null)
		{
			//Restore the fragment's instance
			this.fragment = getSupportFragmentManager().getFragment(savedInstanceState, "fragment");
			this.mTitle = savedInstanceState.getString("title");
			getSupportActionBar().setTitle(this.mTitle);
			this.selectedItemId = savedInstanceState.getInt("selectedItemId");
		}
		else
		{
			selectItem(this.navigationView.getMenu().getItem(0), null);
		}
    }

	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
		}
		else
		{
			this.checkLocationEnabled();
		}
	}

	@Override
	public void onConnectionSuspended(int i)
	{

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		Log.d("eX_lifeCycle", "main => onPostCreate()");
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		this.mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		Log.d("eX_lifeCycle", "main => onConfigurationChanged()");
		super.onConfigurationChanged(newConfig);
		this.mDrawerToggle.onConfigurationChanged(newConfig);
	}

    protected void onStart()
	{
		Log.d("eX_lifeCycle", "main => onStart()");
		// Register broadcast receiver for connectivity changes
		this.connectivityStatusHandler = new ConnectivityStatusHandler(this);
		this.registerReceiver(this.connectivityStatusHandler, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		// Connection to Google API
		if (!this.mGoogleApiClient.isConnected())
		{
			this.mGoogleApiClient.connect();
		}
		super.onStart();
	}

	protected void onStop()
	{
		Log.d("eX_lifeCycle", "main => onStop()");
		this.unregisterReceiver(this.connectivityStatusHandler);
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		Log.d("eX_lifeCycle", "main => onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putString("title", this.mTitle);
		outState.putInt("selectedItemId", this.selectedItemId);
		//Save the fragment's instance
		getSupportFragmentManager().putFragment(outState, "fragment", this.fragment);
	}

	protected void onDestroy(){
		Log.d("eX_lifeCycle", "main => onDestroy()");
		//Stop location service
		if(!isChangingConfigurations()){
			Log.i("explorun_location","Stopping location service");
			Intent intent = new Intent(this, LocationService.class);
			stopService(intent);
		}
		if (this.mGoogleApiClient.isConnected())
		{
			this.mGoogleApiClient.disconnect();
		}
		super.onDestroy();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case MY_PERMISSIONS_REQUEST_GPS:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					checkLocationEnabled();
				}
			}
			break;
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
						// Access to location granted by the user => Start location service
						startService();
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

	public void checkLocationEnabled()
	{
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(LocationService.createLocationRequest());

		PendingResult<LocationSettingsResult> result =
				LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

		result.setResultCallback(new ResultCallback<LocationSettingsResult>()
		{
			@Override
			public void onResult(LocationSettingsResult locationSettingsResult)
			{

				final Status status = locationSettingsResult.getStatus();
				switch (status.getStatusCode())
				{
					case LocationSettingsStatusCodes.SUCCESS:
						// All location settings are satisfied. The client can initialize location requests here.
						startService();
						break;

					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						// Location settings are not satisfied. But could be fixed by showing the user a dialog.
						try
						{
							// Show the dialog by calling startResolutionForResult() and check the result in onActivityResult().
							status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
						}
						catch (IntentSender.SendIntentException e)
						{
							// Ignore the error.
						}
						break;

					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						// Location settings are not satisfied.
						break;
				}
			}
		});
	}

	public void startService()
	{
		Log.i("eX_location","Checking if location service is started...");
		Intent intent = new Intent(this, LocationService.class);
		if(!LocationService.isStarted){
			Log.i("eX_location","Starting Location service");
			this.startService(intent);
		}
		else{
			Log.i("eX_location", "Location service already started");
		}
	}

	public void selectItem(MenuItem item, Bundle args)
	{
		int itemId = item.getItemId();
		if (itemId != this.selectedItemId)
		{
			// Update title and selected item id
			this.mTitle = item.getTitle().toString();
			this.selectedItemId = itemId;

			// Highlight selected item
			item.setChecked(true);

			// Create a new fragment according to id
			switch (itemId)
			{
				case R.id.nav_about:
					this.fragment = new AboutFragment();
					break;

				case R.id.nav_nearby_attractions:
					this.fragment = new NearbyAttractionsFragment();
					break;

				default:
					// Set title for main fragment = app name
					this.mTitle = getResources().getString(R.string.app_name);
					this.fragment = new MapFragment();
					break;
			}

			this.fragment.setArguments(args);
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.container, this.fragment)
					.commit();
		}

		// Close the drawer
		mDrawerLayout.closeDrawers();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event
		return (this.mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
	}

}
