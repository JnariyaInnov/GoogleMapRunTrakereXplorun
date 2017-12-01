package brice.explorun.fragments;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import brice.explorun.R;
import brice.explorun.utilities.SportUtility;

/**
 * Created by germain on 11/28/17.
 */

public class SettingsFragment extends Fragment {

    private SeekBar walkSeekBar, runSeekBar, trailSeekbar;
    private TextView tvWalkSpeed, tvRunSpeed, tvTrailSpeed;
    private static double minWalkSpeed = 2.0;
    private static double minRunSpeed = 5.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        //Retrieve speed settings
        float walkSpeed = SportUtility.getAverageSpeedFromSport(getContext(), SportUtility.WALKING);
        float runSpeed = SportUtility.getAverageSpeedFromSport(getContext(), SportUtility.RUNNING);
        float trailSpeed = SportUtility.getAverageSpeedFromSport(getContext(), SportUtility.TRAIL);
        //Seekbars
        walkSeekBar = view.findViewById(R.id.seekBarWalk);
        walkSeekBar.setProgress((int) (2.0 * (walkSpeed - minWalkSpeed)));
        runSeekBar = view.findViewById(R.id.seekBarRun);
        runSeekBar.setProgress((int) (2.0 * (runSpeed - minRunSpeed)));
        trailSeekbar = view.findViewById(R.id.seekBarTrail);
        trailSeekbar.setProgress((int) (2.0 * (trailSpeed - minRunSpeed)));
        //Speed display
        tvWalkSpeed = view.findViewById(R.id.tv_walkSpeed);
        tvWalkSpeed.setText(walkSpeed + " km/h");
        tvRunSpeed = view.findViewById(R.id.tv_runSpeed);
        tvRunSpeed.setText(runSpeed + " km/h");
        tvTrailSpeed = view.findViewById(R.id.tv_trailSpeed);
        tvTrailSpeed.setText(trailSpeed + " km/h");
        //Add listeners
        addSeekBarListener("walkSpeed", walkSeekBar, tvWalkSpeed);
        addSeekBarListener("runSpeed", runSeekBar, tvRunSpeed);
        addSeekBarListener("trailSpeed", trailSeekbar, tvTrailSpeed);

        return view;
    }

    public void addSeekBarListener(final String prefKey, SeekBar skBar, final TextView txtView){
        skBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double speed;
                //0.5 increments
                if(prefKey.contains("walk")){
                    speed = minWalkSpeed + ((double)progress / 2.0);
                }
                else{
                    speed = minRunSpeed + ((double)progress / 2.0);
                }
                txtView.setText(speed + " km/h");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat(prefKey, (float) speed);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


}
