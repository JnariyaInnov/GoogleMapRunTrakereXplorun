package brice.explorun.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import brice.explorun.R;
import brice.explorun.fragments.HistoryFragment;
import brice.explorun.fragments.MapFragment;
import brice.explorun.fragments.NearbyAttractionsFragment;
import brice.explorun.fragments.SettingsFragment;
import brice.explorun.services.ConnectivityStatusHandler;
import brice.explorun.services.LocationService;
import brice.explorun.services.RouteService;
import brice.explorun.services.TTS;

public class MainActivity extends AppCompatActivity
{
	private Fragment fragment;
	private final int MY_PERMISSIONS_REQUEST_GPS = 0;
	private GoogleSignInClient mGoogleSignInClient;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private NavigationView navigationView;

	private String mTitle;
	private int selectedItemId;

	private ConnectivityStatusHandler connectivityStatusHandler;

	public NavigationView getNavigationView() { return this.navigationView; }

	public ConnectivityStatusHandler getConnectivityStatusHandler() { return this.connectivityStatusHandler; }

	public DrawerLayout getDrawerLayout() { return this.mDrawerLayout; }

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        Log.d("eX_lifeCycle", "main => onCreate()");
        setContentView(R.layout.activity_main);

		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null)
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			this.finish();
		}
		else
		{
			FacebookSdk.sdkInitialize(this.getApplicationContext());
			// Configure Google Sign In
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id))
					.requestEmail()
					.build();

			this.mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

			this.mDrawerLayout = findViewById(R.id.drawer_layout);
			this.navigationView = findViewById(R.id.navigation);

			//setting up selected item listener
			this.navigationView.setNavigationItemSelectedListener(
					new NavigationView.OnNavigationItemSelectedListener()
					{
						@Override
						public boolean onNavigationItemSelected(MenuItem menuItem)
						{
							selectItem(menuItem, null);
							return true;
						}
					});

			this.mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
			{
				/**
				 * Called when a drawer has settled in a completely closed state.
				 */
				public void onDrawerClosed(View view)
				{
					super.onDrawerClosed(view);
					getSupportActionBar().setTitle(mTitle);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}

				/**
				 * Called when a drawer has settled in a completely open state.
				 */
				public void onDrawerOpened(View drawerView)
				{
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

			Intent ttsServiceIntent = new Intent(this, TTS.class);
			if (!TTS.isStarted)
			{
				this.startService(ttsServiceIntent);
			}
		}
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
		if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
		}
		else
		{
			this.checkLocationEnabled();
		}
		super.onStart();
	}

	@Override
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
			Intent intent = new Intent(this, LocationService.class);
			stopService(intent);
			intent = new Intent(this, TTS.class);
			stopService(intent);
			intent = new Intent(this, RouteService.class);
			stopService(intent);
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

	public void checkLocationEnabled()
	{
		if(!isLocationEnabled(this)) {
			openLocationSettings();
		}
		else {
			startService();
		}
	}

	public static boolean isLocationEnabled(Context context) {
		int locationMode = 0;
		String locationProviders;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			try {
				locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

			} catch (Settings.SettingNotFoundException e) {
				e.printStackTrace();
				return false;
			}

			return locationMode != Settings.Secure.LOCATION_MODE_OFF;

		}else{
			locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}


	}

	private void openLocationSettings(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(this.getResources().getString(R.string.no_gps));
		dialog.setPositiveButton(this.getResources().getString(R.string.loc_request), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				getApplicationContext().startActivity(myIntent);
			}
		});
		dialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				Log.e("eX_location","User refused to share location ...");
			}
		});
		dialog.show();
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
			if (itemId == R.id.nav_logout)
			{
				FirebaseAuth.getInstance().signOut();
				this.mGoogleSignInClient.signOut();
				LoginManager.getInstance().logOut();
				Toast.makeText(this, R.string.logout_successful, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				this.finish();
			}
			else
			{
				// Update title and selected item id
				this.mTitle = item.getTitle().toString();
				this.selectedItemId = itemId;

				// Highlight selected item
				item.setChecked(true);

				// Create a new fragment according to id
				switch (itemId)
				{
					case R.id.nav_nearby_attractions:
						this.fragment = new NearbyAttractionsFragment();
						break;

					case R.id.nav_history:
						this.fragment = new HistoryFragment();
						break;

					case R.id.nav_settings:
						this.fragment = new SettingsFragment();
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
		}

		// Close the drawer
		mDrawerLayout.closeDrawers();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			if (this.mDrawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
			{
				Toast.makeText(this, R.string.drawer_locked_error, Toast.LENGTH_SHORT).show();
			}
		}
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event
		return (this.mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
	}

	@Override
	public void onBackPressed()
	{
		if (this.fragment instanceof MapFragment)
		{
			MapFragment fragment = (MapFragment) this.fragment;
			if(!fragment.slideDownFragments())
			{
				this.moveTaskToBack(true);
			}
		}
		else
		{
			this.moveTaskToBack(true);
		}
	}
}
