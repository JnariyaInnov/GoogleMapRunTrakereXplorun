package brice.explorun.utilities;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import brice.explorun.R;

public class TimeUtility
{
	/**
	 * Format a duration in hours and minutes
	 * @param context Context of the call
	 * @param duration Duration to format in minutes
	 * @return A formatted string
	 */
	public static String formatDuration(Context context, int duration)
	{
		String s;

		if (duration < 60){
			s = String.format(context.getString(R.string.form_min_text), duration);
		}
		else {
			long hours = TimeUnit.MINUTES.toHours(duration);
			long minutes = TimeUnit.MINUTES.toMinutes(duration - TimeUnit.HOURS.toMinutes(hours));

			if (minutes != 0) {
				s = String.format(context.getString(R.string.form_h_min_text), hours, minutes);
			}
			else {
				s = String.format(context.getString(R.string.form_h_text), hours);
			}
		}

		return s;
	}

	public static int convertTimeToMinutes(double timeInHours)
	{
		return (int)(timeInHours*60);
	}

	public static String formatTime(long timeInMillis)
	{
		SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
		return format.format(new Date(timeInMillis));
	}

	/**
	 * Compute the estimated time of arrival from actual time and duration in minutes
	 * @param duration Duration of the route in minutes
	 * @return The estimated time of arrival
	 */
	public static String computeEstimatedTimeOfArrival(int duration)
	{
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.add(Calendar.MINUTE, duration);
		return formatTime(calendar.getTimeInMillis());
	}
}
