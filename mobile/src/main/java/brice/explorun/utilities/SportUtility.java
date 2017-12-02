package brice.explorun.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SportUtility
{
	public static final int WALKING = 0;
	public static final int RUNNING = 1;
	public static final int TRAIL = 2;

	// Average speeds for each sport (in km/h)
	private static final float WALKING_SPEED = 3f;
	private static final float RUNNING_SPEED = 9f;
	private static final float TRAIL_SPEED = 11f;

	/**
	 * Function which returns the average speed of a sport
	 * @param sport Sport of the user
	 * @return The average speed of the sport selected by the user
	 */
	public static float getAverageSpeedFromSport(Context context, int sport)
	{
		float res;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		switch (sport)
		{
			case SportUtility.WALKING:
				res = sharedPref.getFloat("walkSpeed", SportUtility.WALKING_SPEED);
				break;

			case SportUtility.RUNNING:
				res = sharedPref.getFloat("runSpeed", SportUtility.RUNNING_SPEED);
				break;

			case SportUtility.TRAIL:
				res = sharedPref.getFloat("trailSpeed", SportUtility.TRAIL_SPEED);
				break;

			default:
				res = -1f;
				Log.e("eX_sportUtility", "Unexpected sport selected");
				break;
		}

		return res;
	}
}
