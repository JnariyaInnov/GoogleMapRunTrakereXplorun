package brice.explorun.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;

import java.util.concurrent.TimeUnit;

import brice.explorun.R;

public class FormFragment extends Fragment implements View.OnClickListener{


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_form,container,false);
		RangeBar mDurationRangeBar = v.findViewById(R.id.fragment_form_duration_range_bar);
		RadioButton mWalkRadioButton = v.findViewById(R.id.fragment_form_walk_radio);
		RadioButton mRunRadioButton = v.findViewById(R.id.fragment_form_run_radio);
		RadioButton mTrailRadioButton = v.findViewById(R.id.fragment_form_trail_radio);
		Button mValidateButton = v.findViewById(R.id.fragment_form_validate_button);
		final TextView mDurationText = v.findViewById(R.id.fragment_form_duration_text);
		mWalkRadioButton.setOnClickListener(this);
		mRunRadioButton.setOnClickListener(this);
		mTrailRadioButton.setOnClickListener(this);
		mValidateButton.setOnClickListener(this);

		mDurationRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,int rightPinIndex, String leftPinValue, String rightPinValue) {
				String s;
				s ="Durée : " + rangeDuration(leftPinValue,rightPinValue);
				mDurationText.setText(s);
			}

		});

		return v;

	}

	@Override
	public void onClick(View v) {
		RangeBar mDurationRangeBar = getView().findViewById(R.id.fragment_form_duration_range_bar);

		String CHOICE = "WALK";
		switch (v.getId()){
			case R.id.fragment_form_walk_radio:
				mDurationRangeBar.setTickEnd(180);
				CHOICE = "WALK";
				break;

			case R.id.fragment_form_run_radio:
				mDurationRangeBar.setTickEnd(180);
				CHOICE = "RUN";
				break;

			case R.id.fragment_form_trail_radio:
				mDurationRangeBar.setTickEnd(300);
				CHOICE = "TRAIL";
				break;

			case R.id.fragment_form_validate_button:
				//TODO Return result for another fragment.
				getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
				break;


		}
	}

	public String rangeDuration(String leftPin, String rightPin){
		String s;
		int leftPinInt = Integer.parseInt(leftPin);
		int rightPinInt = Integer.parseInt(rightPin);

		if (leftPinInt < 60 ){
			s = String.format("%02d", leftPinInt) + " min ";
		}
		else{
			long hours = TimeUnit.MINUTES.toHours(leftPinInt);
			s = String.format("%02d", hours) + " H " ;
			long minutes = TimeUnit.MINUTES.toMinutes(leftPinInt - TimeUnit.HOURS.toMinutes(hours));
			if (minutes != 0){
				s +=  String.format("%02d", minutes) + " min " ;
			}
		}

		if (rightPinInt < 60 ){
			s +="- " + String.format("%02d", rightPinInt) + " min";
		}
		else{
			long hours = TimeUnit.MINUTES.toHours(rightPinInt);
			s +="- " + String.format("%02d", hours) + " H ";
			long minutes = TimeUnit.MINUTES.toMinutes(rightPinInt - TimeUnit.HOURS.toMinutes(hours));
			if (minutes != 0){
				s +=  String.format("%02d", minutes) + " min" ;
			}
		}
		return s;
	}

}
