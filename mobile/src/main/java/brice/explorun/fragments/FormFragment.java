package brice.explorun.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;

import java.util.concurrent.TimeUnit;

import brice.explorun.R;
import brice.explorun.models.FormObserver;
import brice.explorun.models.Utility;
import brice.explorun.models.Utility.*;

public class FormFragment extends Fragment implements View.OnClickListener{

	private RangeBar mDurationRangeBar;
	private RadioButton mWalkRadioButton;
	private RadioButton mRunRadioButton;
	private RadioButton mTrailRadioButton;
	private Button mValidateButton;
	private TextView mDurationText;

	private Animation animation;

	private SPORTS chosenSport;

	private FormObserver observer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_form,container,false);

		mDurationRangeBar = v.findViewById(R.id.fragment_form_duration_range_bar);
		mWalkRadioButton = v.findViewById(R.id.fragment_form_walk_radio);
		mRunRadioButton = v.findViewById(R.id.fragment_form_run_radio);
		mTrailRadioButton = v.findViewById(R.id.fragment_form_trail_radio);
		mValidateButton = v.findViewById(R.id.fragment_form_validate_button);
		mDurationText = v.findViewById(R.id.fragment_form_duration_text);

		mWalkRadioButton.setOnClickListener(this);
		mRunRadioButton.setOnClickListener(this);
		mTrailRadioButton.setOnClickListener(this);
		mValidateButton.setOnClickListener(this);

		mDurationText.setText(String.format(getResources().getString(R.string.form_duration_text),rangeDuration(mDurationRangeBar.getLeftPinValue()),rangeDuration(mDurationRangeBar.getRightPinValue())));

		mDurationRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,int rightPinIndex, String leftPinValue, String rightPinValue) {
				mDurationText.setText(String.format(getResources().getString(R.string.form_duration_text),rangeDuration(leftPinValue),rangeDuration(rightPinValue)));
			}

		});

		this.chosenSport = SPORTS.WALK;

		try
		{
			this.observer = (FormObserver) getParentFragment();
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(getParentFragment().toString() + " must implement FormObserver");
		}

		return v;

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.fragment_form_walk_radio:
				mDurationRangeBar.setTickEnd(180);
				this.chosenSport = SPORTS.WALK;
				break;

			case R.id.fragment_form_run_radio:
				mDurationRangeBar.setTickEnd(180);
				this.chosenSport = SPORTS.RUNNING;
				break;

			case R.id.fragment_form_trail_radio:
				mDurationRangeBar.setTickEnd(300);
				this.chosenSport = SPORTS.TRAIL;
				break;

			case R.id.fragment_form_validate_button:
				if (this.observer != null)
				{
					this.observer.onFormValidate(this.chosenSport, this.mDurationRangeBar.getLeftPinValue(), this.mDurationRangeBar.getRightPinValue());
				}
				break;
		}
	}

	public String rangeDuration(String Pin){
		String s;
		int leftPinInt = Integer.parseInt(Pin);

		if (leftPinInt < 60 ){
			s = String.format(getString(R.string.form_min_text), leftPinInt);
		}
		else {
			long hours = TimeUnit.MINUTES.toHours(leftPinInt);
			long minutes = TimeUnit.MINUTES.toMinutes(leftPinInt - TimeUnit.HOURS.toMinutes(hours));

			if (minutes != 0) {
				s = String.format(getString(R.string.form_h_min_text), hours, minutes);
			}
			else {
				s = String.format(getString(R.string.form_h_text), hours);
			}
		}

		return s;
	}
}
