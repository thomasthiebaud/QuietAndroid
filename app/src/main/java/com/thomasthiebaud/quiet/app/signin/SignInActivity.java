package com.thomasthiebaud.quiet.app.signin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.thomasthiebaud.quiet.BuildConfig;
import com.thomasthiebaud.quiet.app.error.ErrorActivity;
import com.thomasthiebaud.quiet.app.success.SuccessActivity;
import com.thomasthiebaud.quiet.contracts.SignInContract;
import com.thomasthiebaud.quiet.services.HttpService;
import com.thomasthiebaud.quiet.services.Message;
import com.thomasthiebaud.quiet.R;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = SignInActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.QUIET_SERVER_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SignInContract.SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            HashMap<String, Object> body = new HashMap<>();
            body.put("idToken", acct.getIdToken());

            Call<Message> results = HttpService.getInstance().getQuietApi().signIn(body);
            results.enqueue(new Callback<Message>() {
                @Override
                public void onResponse(Call<Message> call, Response<Message> response) {
                    Log.w(TAG, String.valueOf(response.body().getMessage()));
                    Intent intent = new Intent(getBaseContext(), SuccessActivity.class);
                    intent.putExtra("title", "Success");
                    intent.putExtra("description", "The app is running successfully");
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<Message> call, Throwable t) {
                    Log.e(TAG, t.toString());
                    Intent intent = new Intent(getBaseContext(), ErrorActivity.class);
                    intent.putExtra("title", "Error");
                    intent.putExtra("description", t.getMessage());
                    startActivity(intent);
                }
            });
        } else {
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SignInContract.SIGN_IN_REQUEST_CODE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Intent intent = new Intent(getBaseContext(), ErrorActivity.class);
        intent.putExtra("title", "Error");
        intent.putExtra("description", "Google connection failed");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }
}
