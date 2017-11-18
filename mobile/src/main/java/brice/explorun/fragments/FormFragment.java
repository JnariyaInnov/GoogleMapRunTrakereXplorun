package brice.explorun.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import brice.explorun.models.FormObserver;
import brice.explorun.models.Utility.*;

public class FormFragment extends Fragment implements View.OnClickListener{

	private RangeBar mDurationRangeBar;
	private RadioButton mWalkRadioButton;
	private RadioButton mRunRadioButton;
	private RadioButton mTrailRadioButton;
	private Button mValidateButton;
	private TextView mDurationText;

	private int chosenSport;

	private FormObserver observer;

	private SharedPreferences sharedPref;

	private final int DEFAULT_MAX_RANGE = 180;
	private final int TRAIL_MAX_RANGE = 300;

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

		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		this.chosenSport = this.sharedPref.getInt("sport", Sport.WALKING);

		checkRadioButton();

		mDurationText.setText(String.format(getResources().getString(R.string.form_duration_text),rangeDuration(Integer.parseInt(mDurationRangeBar.getLeftPinValue())),rangeDuration(Integer.parseInt(mDurationRangeBar.getRightPinValue()))));

		mDurationRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,int rightPinIndex, String leftPinValue, String rightPinValue) {
				int leftValue = getValidValue(leftPinValue);
				int rightValue = getValidValue(rightPinValue);
				mDurationText.setText(String.format(getResources().getString(R.string.form_duration_text),rangeDuration(leftValue),rangeDuration(rightValue)));
			}

		});

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

	public void checkRadioButton()
	{
		switch (this.chosenSport)
		{
			case Sport.TRAIL:
				mDurationRangeBar.setTickEnd(TRAIL_MAX_RANGE);
				this.mTrailRadioButton.setChecked(true);
				break;

			case Sport.RUNNING:
				mDurationRangeBar.setTickEnd(DEFAULT_MAX_RANGE);
				this.mRunRadioButton.setChecked(true);
				break;

			default:
				mDurationRangeBar.setTickEnd(DEFAULT_MAX_RANGE);
				this.mWalkRadioButton.setChecked(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.fragment_form_walk_radio:
				mDurationRangeBar.setTickEnd(DEFAULT_MAX_RANGE);
				this.chosenSport = Sport.WALKING;
				break;

			case R.id.fragment_form_run_radio:
				mDurationRangeBar.setTickEnd(DEFAULT_MAX_RANGE);
				this.chosenSport = Sport.RUNNING;
				break;

			case R.id.fragment_form_trail_radio:
				mDurationRangeBar.setTickEnd(TRAIL_MAX_RANGE);
				this.chosenSport = Sport.TRAIL;
				break;

			case R.id.fragment_form_validate_button:
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit();
				editor.putInt("sport", this.chosenSport);
				editor.apply();
				if (this.observer != null)
				{
					this.observer.onFormValidate(this.chosenSport, getValidValue(this.mDurationRangeBar.getLeftPinValue()), getValidValue(this.mDurationRangeBar.getRightPinValue()));
				}
				break;
		}
	}

	public String rangeDuration(int pinInt){
		String s;

		if (pinInt < 60){
			s = String.format(getString(R.string.form_min_text), pinInt);
		}
		else {
			long hours = TimeUnit.MINUTES.toHours(pinInt);
			long minutes = TimeUnit.MINUTES.toMinutes(pinInt - TimeUnit.HOURS.toMinutes(hours));

			if (minutes != 0) {
				s = String.format(getString(R.string.form_h_min_text), hours, minutes);
			}
			else {
				s = String.format(getString(R.string.form_h_text), hours);
			}
		}

		return s;
	}

	public int getValidValue(String pinValue)
	{
		int pinInt = Integer.parseInt(pinValue);

		if (chosenSport == Sport.TRAIL)
		{
			if (pinInt > TRAIL_MAX_RANGE)
			{
				pinInt = TRAIL_MAX_RANGE;
			}
		}
		else
		{
			if (pinInt > DEFAULT_MAX_RANGE)
			{
				pinInt = DEFAULT_MAX_RANGE;
			}
		}

		if (pinInt < 10)
		{
			pinInt = 10;
		}

		return pinInt;
	}
}
