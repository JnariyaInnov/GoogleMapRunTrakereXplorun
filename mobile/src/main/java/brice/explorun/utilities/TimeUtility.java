package brice.explorun.utilities;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

	public static String formatTime(Context context, long timeInMillis)
	{
		return DateUtils.formatDateTime(context, timeInMillis, DateUtils.FORMAT_SHOW_TIME);
	}

	/**
	 * Format time in h:m:s format
	 * @param timeInMillis Time in milliseconds
	 * @return The formatted time
	 */
	public static String formatDurationHms(Context context, long timeInMillis)
	{
		timeInMillis /= 1000;
		int seconds = (int) (timeInMillis % 60);
		timeInMillis /= 60;
		int minutes = (int) (timeInMillis % 60);
		timeInMillis /= 60;
		int hours = (int) (timeInMillis % 24);
		return String.format(context.getString(R.string.duration_text), hours, minutes, seconds);
	}

	/**
	 * Compute the estimated time of arrival from actual time and duration in minutes
	 * @param duration Duration of the route in minutes
	 * @return The estimated time of arrival
	 */
	public static String computeEstimatedTimeOfArrival(Context context, int duration)
	{
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.add(Calendar.MINUTE, duration);
		return formatTime(context, calendar.getTimeInMillis());
	}

	/**
	 * Compute the average speed of the user
	 * @param distance Distance in kilometers
	 * @param timeInMillis Elapsed time in ms
	 * @return The average speed in km/h
	 */
	public static double computeAverageSpeed(double distance, long timeInMillis)
	{
		double hours = (timeInMillis/1000.0)/3600.0;
		return distance/hours;
	}

	public static String formatDate(Context context, Date date)
	{
		return DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
	}
}
