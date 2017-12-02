package brice.explorun.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import brice.explorun.R;
import brice.explorun.activities.MainActivity;
import brice.explorun.adapters.RoutesHistoryAdapter;
import brice.explorun.models.FirebaseRoute;
import brice.explorun.utilities.Utility;

public class HistoryFragment extends Fragment
{
	private final String ROUTES_KEY = "routes";

	private ArrayList<FirebaseRoute> routes;
	private RoutesHistoryAdapter adapter;

	private LinearLayout progressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_history, container, false);

		this.progressBar = view.findViewById(R.id.progress_layout);

		if (savedInstanceState != null)
		{
			this.routes = savedInstanceState.getParcelableArrayList(ROUTES_KEY);
		}
		else
		{
			this.routes = new ArrayList<>();
		}

		ListView list = view.findViewById(R.id.list_history);
		this.adapter = new RoutesHistoryAdapter(this.getActivity(), this.routes);
		list.setAdapter(this.adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				viewRouteOnMap(routes.get(i));
			}
		});

		// We don't retrieve the user's history when he has just changed the orientation
		if (savedInstanceState == null)
		{
			getRoutesHistory();
		}

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		MainActivity activity = (MainActivity) this.getActivity();
		activity.getConnectivityStatusHandler().updateNoNetworkLabel();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelableArrayList(ROUTES_KEY, this.routes);
		super.onSaveInstanceState(outState);
	}

	public void getRoutesHistory()
	{
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		if (user != null)
		{
			this.progressBar.setVisibility(View.VISIBLE);
			Log.d("HistoryFragment", "Retrieving history");
			// We set a listener to be able to retrieve the user's history even if he is offline
			db.collection("users").document(user.getUid()).collection("routes").orderBy("date", Query.Direction.DESCENDING)
			.addSnapshotListener(new EventListener<QuerySnapshot>()
			{
				@Override
				public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException e)
				{
					if (e != null)
					{
						Log.e("HistoryFragment", "Listen error", e);
						progressBar.setVisibility(View.GONE);
						Toast.makeText(getActivity(), R.string.history_error, Toast.LENGTH_SHORT).show();
					}
					else
					{
						Log.d("HistoryFragment", "Getting routes");
						adapter.clear();
						adapter.addAll(querySnapshot.toObjects(FirebaseRoute.class));
						progressBar.setVisibility(View.GONE);
					}
				}
			});
		}
	}

	public void viewRouteOnMap(FirebaseRoute route)
	{
		// To view the route on the map, the user needs an internet connection because of the call to Google Directions API
		if (Utility.isOnline(this.getActivity()))
		{
			Bundle args = new Bundle();
			args.putParcelable("route", route);
			// Setting the new fragment in MainActivity
			MainActivity activity = (MainActivity) this.getActivity();
			activity.getSupportActionBar().setTitle(activity.getResources().getString(R.string.app_name));
			activity.selectItem(activity.getNavigationView().getMenu().getItem(0), args);
		}
		else
		{
			Toast.makeText(this.getActivity(), R.string.view_route_on_map_error, Toast.LENGTH_SHORT).show();
		}
	}
}
