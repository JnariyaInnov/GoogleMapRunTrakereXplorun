package brice.explorun.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
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
		if (Utility.isOnline(this.getActivity()))
		{
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			FirebaseFirestore db = FirebaseFirestore.getInstance();
			if (user != null)
			{
				this.progressBar.setVisibility(View.VISIBLE);
				Log.d("HistoryFragment", "Retrieving history");
				db.collection("users").document(user.getUid()).collection("routes").orderBy("date", Query.Direction.DESCENDING)
				.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<QuerySnapshot> task)
					{
						if (task.isSuccessful())
						{
							adapter.clear();
							for (DocumentSnapshot document : task.getResult())
							{
								adapter.add(document.toObject(FirebaseRoute.class));
							}
							progressBar.setVisibility(View.GONE);
						}
						else
						{
							progressBar.setVisibility(View.GONE);
							Toast.makeText(getActivity(), R.string.history_error, Toast.LENGTH_SHORT).show();
							Log.e("HistoryFragment", "error retrieving routes: ", task.getException());
						}
					}
				});
			}
		}
	}
}
