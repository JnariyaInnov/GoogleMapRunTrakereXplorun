package brice.explorun.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import brice.explorun.R;

public class LocationUtility
{
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

	public static float[] getLocationFromPreferences(Context context)
	{
		float[] res = new float[2];
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		res[0] = sharedPref.getFloat("latitude", -1);
		res[1] = sharedPref.getFloat("longitude", -1);
		return res;
	}

	public static String formatDistance(Context context, double distance)
	{
		String res;
		if (distance < 1000)
		{
			// We round the distance to the nearest multiple of 10
			distance = Math.round(distance/10.0)*10.0;
			res = String.format(context.getResources().getString(R.string.distance_in_m), distance);
		}
		else
		{
			distance = Math.round(distance/1000*10.0)/10.0;
			res = String.format(context.getResources().getString(R.string.distance_in_km), distance);
		}
		return res;
	}
}
