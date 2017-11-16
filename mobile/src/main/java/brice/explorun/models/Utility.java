package brice.explorun.models;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import brice.explorun.R;

public class Utility
{
	// Class to define the sports
	public static class Sport {
		public static final int WALKING = 0;
		public static final int RUNNING = 1;
		public static final int TRAIL = 2;

		// Average speeds for each sport (in km/h)
		private static final int WALKING_SPEED = 3;
		private static final int RUNNING_SPEED = 9;
		private static final int TRAIL_SPEED = 11;
	}


	/**
	 * Method to know if the user is connected to the Internet
	 *
	 * @param context Context of the application
	 * @return True if the user is connected to the Internet, else false
	 */
	public static boolean isOnline(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
		{
			return false;
		}
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnectedOrConnecting());
	}

	public static AlertDialog showAlertDialog(int titleId, int messageId, Context context)
	{
		AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
		}
		else
		{
			builder = new AlertDialog.Builder(context);
		}
		return builder.setTitle(context.getResources().getString(titleId))
				.setMessage(context.getResources().getString(messageId))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						// continue with delete
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	/**
	 * Function which converts degrees in radians
	 * @param degrees Value in degrees to convert
	 * @return The converted value in radians
	 */
	private static double degreesToRadians(double degrees)
	{
		return degrees * Math.PI / 180;
	}

	/**
	 * Function which computes the distance between two GPS coordinates
	 * @param lat1 First latitude
	 * @param lon1 First longitude
	 * @param lat2 Second latitude
	 * @param lon2 Second longitude
	 * @return The distance between the two GPS coordinates, in km
	 */
	public static double distanceBetweenCoordinates(double lat1, double lon1, double lat2, double lon2)
	{
		int earthRadiusKm = 6371;

		double dLat = degreesToRadians(lat2 - lat1);
		double dLon = degreesToRadians(lon2 - lon1);

		lat1 = degreesToRadians(lat1);
		lat2 = degreesToRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return earthRadiusKm * c;
	}

	public static float getPlaceMarkerColor(Fragment context, Place place)
	{
		List<String> appTypes = Arrays.asList(context.getResources().getStringArray(R.array.places_types));
		ArrayList<String> types = place.getTypes();
		int i = 0;
		while(i < types.size() && !appTypes.contains(types.get(i)))
		{
			i++;
		}

		if (i == types.size())
		{
			return Utility.getColorFromType(context, "");
		}
		else
		{
			return Utility.getColorFromType(context, types.get(i));
		}
	}

	private static float getColorFromType(Fragment context, String type)
	{
		float res;
		switch (type)
		{
			case "park":
				res = colorToHue(context, R.color.dark_green);
				break;

			case "museum":
				res = BitmapDescriptorFactory.HUE_ORANGE;
				break;

			case "city_hall":
			case "church":
				res = colorToHue(context, R.color.brown);
				break;

			default:
				res = BitmapDescriptorFactory.HUE_RED;
				break;
		}

		return res;
	}

	private static float colorToHue(Fragment context, int colorRes)
	{
		float hsv[] = new float[3];
		if (context != null)
		{
			Color.colorToHSV(context.getResources().getColor(colorRes), hsv);
			return hsv[0];
		}
		else
		{
			return 0;
		}
	}

	public static float[] getLocationFromPreferences(Context context)
	{
		float[] res = new float[2];
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		res[0] = sharedPref.getFloat("latitude", -1);
		res[1] = sharedPref.getFloat("longitude", -1);
		return res;
	}

	/**
	 * Function which returns the average speed of a sport
	 * @param sport Sport of the user
	 * @return The average speed of the sport selected by the user
	 */
	public static int getAverageSpeedFromSport(int sport)
	{
		int res;
		switch (sport)
		{
			case Sport.TRAIL:
				res = Sport.TRAIL_SPEED;
				break;

			case Sport.RUNNING:
				res = Sport.RUNNING_SPEED;
				break;

			default:
				res = Sport.WALKING_SPEED;
				break;
		}

		return res;
	}

	public static CameraUpdate getCameraUpdateBounds(int width, int height, int padding, List<LatLng> points)
	{
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LatLng point: points)
		{
			builder.include(point);
		}

		LatLngBounds bounds = builder.build();

		return CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
	}
}
