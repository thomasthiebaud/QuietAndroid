package com.thomasthiebaud.quiet.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.thomasthiebaud.quiet.BuildConfig;
import com.thomasthiebaud.quiet.contract.IntentContract;
import com.thomasthiebaud.quiet.utils.Body;
import com.thomasthiebaud.quiet.contract.SignInContract;
import com.thomasthiebaud.quiet.services.HttpService;
import com.thomasthiebaud.quiet.model.Message;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = SignInActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        tracker = application.getDefaultTracker();

        Dexter.initialize(this);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.QUIET_SERVER_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
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
    public void onResume() {
        super.onResume();
        tracker.setScreenName("SignIn : " + SignInActivity.class.getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();

            Body body = new Body().add(SignInContract.ID_TOKEN, acct.getIdToken());

            Call<Message> results = HttpService.getInstance().getQuietApi().signIn(body);
            results.enqueue(new Callback<Message>() {
                @Override
                public void onResponse(Call<Message> call, Response<Message> response) {
                    Utils.saveIdToken(getApplicationContext(), acct.getIdToken());
                    checkPermission();
                }

                @Override
                public void onFailure(Call<Message> call, Throwable t) {
                    Log.e(TAG, t.toString());
                    String title = getResources().getString(R.string.error);
                    DisplayActivity.displayError(getApplicationContext(), title, t.getMessage());
                }
            });
        } else {
            //DisplayActivity.displayError(getApplicationContext(), "Impossible to signIn");
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

    private void checkPermission() {
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()) {
                    Context context = getApplicationContext();
                    Utils.isRunning(context, true);
                    Utils.updateWidget(context, R.drawable.running, context.getString(R.string.quiet_is_running));

                    String title = getResources().getString(R.string.running_app_title);
                    String description = getResources().getString(R.string.running_app_description);
                    DisplayActivity.displaySuccess(getApplicationContext(),title, description);
                } else {
                    String description = getResources().getString(R.string.permission_denied_description);
                    Snackbar.make(findViewById(R.id.rootView), description, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SignInContract.SIGN_IN_REQUEST_CODE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        String title = getResources().getString(R.string.error);
        String description = getResources().getString(R.string.connection_failed);
        DisplayActivity.displayError(getApplicationContext(), title, description);
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
