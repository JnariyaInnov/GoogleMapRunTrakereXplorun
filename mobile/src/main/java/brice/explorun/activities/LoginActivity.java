package brice.explorun.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import brice.explorun.R;

public class LoginActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(this.getApplicationContext());
		AppEventsLogger.activateApp(this);
		setContentView(R.layout.activity_login);
	}
}
