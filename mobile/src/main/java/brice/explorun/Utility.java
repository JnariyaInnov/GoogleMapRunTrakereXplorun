package brice.explorun;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;

public class Utility
{
	public static final int CHECK_INTERVAL = 5000; // Time between two checks of internet connection (in ms)

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

	public static AlertDialog showAlertDialog(int titleId, int messageId, Context context)
	{
		AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
		} else {
			builder = new AlertDialog.Builder(context);
		}
		return builder.setTitle(context.getResources().getString(titleId))
				.setMessage(context.getResources().getString(messageId))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// continue with delete
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	public static ProgressBar showProgressBar(Context context)
	{
		return new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
	}
}
