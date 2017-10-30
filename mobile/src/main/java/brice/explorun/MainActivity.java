package brice.explorun;

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
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
	private Fragment fragment;
	private final int REQUEST_CHECK_SETTINGS = 0x1;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private String mTitle;
	private int selectedItemId;

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		this.mDrawerLayout = findViewById(R.id.drawer_layout);
		NavigationView navigationView =  findViewById(R.id.navigation);

		//setting up selected item listener
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						selectItem(menuItem);
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
			this.selectedItemId = savedInstanceState.getInt("selectedItemId");
		}
		else
		{
			selectItem(navigationView.getMenu().getItem(0));
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
						if (this.fragment instanceof MapsFragment)
						{
							MapsFragment fragment = (MapsFragment) this.fragment;
							fragment.getLocationManager().getLocation();
						}
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
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		this.mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		this.mDrawerToggle.onConfigurationChanged(newConfig);
	}


	public void selectItem(MenuItem item)
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

				default:
					// Set title for main fragment = app name
					this.mTitle = getResources().getString(R.string.app_name);
					this.fragment = new MapsFragment();
					break;
			}

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
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (this.mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("title", this.mTitle);
		outState.putInt("selectedItemId", this.selectedItemId);
		//Save the fragment's instance
		getSupportFragmentManager().putFragment(outState, "fragment", this.fragment);
	}

	protected void onDestroy()
	{
		super.onDestroy();
	}
}
