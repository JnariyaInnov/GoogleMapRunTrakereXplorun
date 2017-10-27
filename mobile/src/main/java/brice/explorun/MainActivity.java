package brice.explorun;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
	private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			//Restore the fragment's instance
			this.fragment = getSupportFragmentManager().getFragment(savedInstanceState, "mapFragment");
		}
		else {
			this.fragment = new MapsFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, this.fragment).commit();
		}
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//Save the fragment's instance
		getSupportFragmentManager().putFragment(outState, "mapFragment", this.fragment);
	}
}
