package brice.explorun.models;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class CustomRequestQueue
{
	private RequestQueue mRequestQueue = null;
	private Context context;

	private static CustomRequestQueue instance;

	private CustomRequestQueue(Context context)
	{
		this.context = context;
		this.mRequestQueue = getRequestQueue();
	}

	public static synchronized CustomRequestQueue getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new CustomRequestQueue(context);
		}
		return instance;
	}

	public RequestQueue getRequestQueue()
	{
		if (this.mRequestQueue == null)
		{
			// getApplicationContext() is key, it keeps you from leaking the
			// Activity or BroadcastReceiver if someone passes one in.
			this.mRequestQueue = Volley.newRequestQueue(this.context.getApplicationContext());
		}
		return this.mRequestQueue;
	}

	public <T> void addRequestToQueue(Request<T> req)
	{
		getRequestQueue().add(req);
	}

	public <T> void addRequestListToQueue(List<Request<T>> list)
	{
		for (Request <T> req: list)
		{
			addRequestToQueue(req);
		}
	}
}
