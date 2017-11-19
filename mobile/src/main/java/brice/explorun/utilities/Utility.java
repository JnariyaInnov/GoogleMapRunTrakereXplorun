package brice.explorun.utilities;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import brice.explorun.R;
import brice.explorun.models.Place;

public class Utility
{
	/**
	 * Method to know if the user is connected to the Internet
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
				res = colorToHue(context, R.color.darkGreen);
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
		if (context.getActivity() != null)
		{
			Color.colorToHSV(context.getActivity().getResources().getColor(colorRes), hsv);
			return hsv[0];
		}
		else
		{
			return 0;
		}
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
