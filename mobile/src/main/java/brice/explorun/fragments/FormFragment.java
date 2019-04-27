package brice.explorun.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;

import brice.explorun.R;
import brice.explorun.models.RouteObserver;
import brice.explorun.utilities.SportUtility;
import brice.explorun.utilities.TimeUtility;

public class FormFragment extends Fragment implements View.OnClickListener{

	private RangeBar mDurationRangeBar;
	private RadioButton mWalkRadioButton;
	private RadioButton mRunRadioButton;
	private RadioButton mTrailRadioButton;
	private Button mValidateButton;
	private Button mCancelButton;
	private TextView mDurationText;

	private ScrollView layout;
	private Animation animation;

	private int chosenSport;

	private RouteObserver observer;

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
		mCancelButton = v.findViewById(R.id.fragment_form_cancel_button);
		mDurationText = v.findViewById(R.id.fragment_form_duration_text);

		mWalkRadioButton.setOnClickListener(this);
		mRunRadioButton.setOnClickListener(this);
		mTrailRadioButton.setOnClickListener(this);
		mValidateButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);

		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		this.chosenSport = this.sharedPref.getInt("sport", SportUtility.WALKING);

		checkRadioButton();

		int leftValue = Integer.parseInt(mDurationRangeBar.getLeftPinValue());
		int rightValue = Integer.parseInt(mDurationRangeBar.getRightPinValue());
		mDurationText.setText(String.format(getResources().getString(R.string.form_duration_text), TimeUtility.formatDuration(this.getActivity(), leftValue), TimeUtility.formatDuration(this.getActivity(), rightValue)));

		mDurationRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,int rightPinIndex, String leftPinValue, String rightPinValue) {
				int leftValue = getValidValue(leftPinValue);
				int rightValue = getValidValue(rightPinValue);
				mDurationText.setText(String.format(getResources().getString(R.string.form_duration_text),TimeUtility.formatDuration(getActivity(), leftValue),TimeUtility.formatDuration(getActivity(), rightValue)));
			}

		});

		try
		{
			this.observer = (RouteObserver) getParentFragment();
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(getParentFragment().toString() + " must implement RouteObserver");
		}

		this.layout = v.findViewById(R.id.form);
		this.animation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_down);

		return v;

	}

	public void checkRadioButton()
	{
		switch (this.chosenSport)
		{
			case SportUtility.TRAIL:
				mDurationRangeBar.setTickEnd(TRAIL_MAX_RANGE);
				this.mTrailRadioButton.setChecked(true);
				break;

			case SportUtility.RUNNING:
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
				this.chosenSport = SportUtility.WALKING;
				break;

			case R.id.fragment_form_run_radio:
				mDurationRangeBar.setTickEnd(DEFAULT_MAX_RANGE);
				this.chosenSport = SportUtility.RUNNING;
				break;

			case R.id.fragment_form_trail_radio:
				mDurationRangeBar.setTickEnd(TRAIL_MAX_RANGE);
				this.chosenSport = SportUtility.TRAIL;
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

			case R.id.fragment_form_cancel_button:
				this.layout.setAnimation(this.animation);
				this.layout.setVisibility(View.GONE);
				this.layout.startAnimation(this.animation);
				break;

		}
	}

	public int getValidValue(String pinValue)
	{
		int pinInt = Integer.parseInt(pinValue);

		if (chosenSport == SportUtility.TRAIL)
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
