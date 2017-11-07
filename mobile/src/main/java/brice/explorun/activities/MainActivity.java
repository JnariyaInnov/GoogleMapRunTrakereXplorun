package brice.explorun.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import brice.explorun.R;
import brice.explorun.fragments.AboutFragment;
import brice.explorun.fragments.MapFragment;
import brice.explorun.fragments.NearbyAttractionsFragment;
import brice.explorun.services.ConnectivityStatusHandler;
import brice.explorun.services.LocationService;

public class MainActivity extends AppCompatActivity
{
	private Fragment fragment;
	private final int REQUEST_CHECK_SETTINGS = 0x1;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private NavigationView navigationView;

	private String mTitle;
	private int selectedItemId;

	private ConnectivityStatusHandler connectivityStatusHandler;

	public NavigationView getNavigationView() { return this.navigationView; }

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        Log.d("eX_lifeCycle", "main => onCreate()");
        setContentView(R.layout.activity_main);

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

    protected void onDestroy(){
		Log.d("eX_lifeCycle", "main => onDestroy()");
		//Stop location service
		if(!isChangingConfigurations()){
			Log.i("explorun_location","Stopping location service");
			Intent intent = new Intent(this, LocationService.class);
			stopService(intent);
		}
		super.onDestroy();
	}

    protected void onStart()
	{
		Log.d("eX_lifeCycle", "main => onStart()");
		this.connectivityStatusHandler = ConnectivityStatusHandler.getInstance(this);
		this.connectivityStatusHandler.run();

		super.onStart();
	}

	protected void onStop()
	{
		Log.d("eX_lifeCycle", "main => onStop()");
		this.connectivityStatusHandler.stopThread();

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
						Log.i("eX_location","Starting location service");
						Intent intent = new Intent(this, LocationService.class);
						startService(intent);
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
