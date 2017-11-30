package brice.explorun.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import brice.explorun.R;
import brice.explorun.activities.MainActivity;
import brice.explorun.models.CustomRoute;
import brice.explorun.utilities.Utility;

public class LoginFragment extends Fragment
{
	private final int GOOGLE_SIGN_IN_RESULT = 0;

	private GoogleSignInClient mGoogleSignInClient;
	private FirebaseAuth mAuth;
	private CallbackManager callbackManager;

	private RelativeLayout layout;
	private ProgressBar progressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_login, container, false);

		SignInButton googleSignInButton = view.findViewById(R.id.google_sign_in_button);
		googleSignInButton.setSize(SignInButton.SIZE_WIDE);
		googleSignInButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				signInWithGoogle();
			}
		});

		LoginButton loginButton = view.findViewById(R.id.facebook_sign_in_button);
		loginButton.setReadPermissions("email", "public_profile");
		// If using in a fragment
		loginButton.setFragment(this);
		// Other app specific specialization

		// Callback registration
		this.callbackManager = CallbackManager.Factory.create();
		loginButton.registerCallback(this.callbackManager, new FacebookCallback<LoginResult>()
		{
			@Override
			public void onSuccess(LoginResult loginResult)
			{
				Log.d("LoginFragment", "facebook:onSuccess:" + loginResult);
				signInFirebase(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()));
			}

			@Override
			public void onCancel()
			{
				Toast.makeText(getActivity(), R.string.login_error_text, Toast.LENGTH_LONG).show();
				Log.d("LoginFragment", "facebook:onCancel");
			}

			@Override
			public void onError(FacebookException exception)
			{
				Toast.makeText(getActivity(), R.string.login_error_text, Toast.LENGTH_LONG).show();
				Log.d("LoginFragment", "facebook:onError", exception);
			}
		});

		// Configure Google Sign In
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();

		this.mGoogleSignInClient = GoogleSignIn.getClient(this.getActivity(), gso);

		this.mAuth = FirebaseAuth.getInstance();

		this.layout = view.findViewById(R.id.login_layout);
		this.progressBar = view.findViewById(R.id.progress_bar);

		return view;
	}

	public void signInWithGoogle()
	{
		if (Utility.isOnline(this.getActivity()))
		{
			Intent signInIntent = mGoogleSignInClient.getSignInIntent();
			startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RESULT);
		}
		else
		{
			Toast.makeText(this.getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == GOOGLE_SIGN_IN_RESULT)
		{
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try
			{
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				signInFirebase(GoogleAuthProvider.getCredential(account.getIdToken(), null));
			}
			catch (ApiException e)
			{
				// Google Sign In failed, update UI appropriately
				Toast.makeText(this.getActivity(), R.string.login_error_text, Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			this.callbackManager.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void signInFirebase(AuthCredential credential)
	{
		this.showProgressBar();
		this.mAuth.signInWithCredential(credential).addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>()
		{
			@Override
			public void onComplete(@NonNull Task<AuthResult> task)
			{
				hideProgressBar();
				if (task.isSuccessful())
				{
					// Sign in success, update UI with the signed-in user's information
					Log.d("LoginFragment", "SignInWithCredential:success");
					Toast.makeText(getActivity(), R.string.login_successful, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getActivity(), MainActivity.class);
					startActivity(intent);
					getActivity().finish();
				}
				else
				{
					// If sign in fails, display a message to the user.
					Log.w("LoginFragment", "SignInWithCredential:failure", task.getException());
					Toast.makeText(getActivity(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void showProgressBar()
	{
		this.layout.setAlpha(0.4f);
		this.progressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar()
	{
		this.progressBar.setVisibility(View.GONE);
		this.layout.setAlpha(1.0f);
	}
}
