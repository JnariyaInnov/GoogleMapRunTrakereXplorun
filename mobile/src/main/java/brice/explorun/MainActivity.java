package brice.explorun;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment fragment = new MapsFragment();
        //fragment.getMapAsync(fragment);
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
    }
}
