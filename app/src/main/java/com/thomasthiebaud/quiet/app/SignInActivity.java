package com.thomasthiebaud.quiet.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.thomasthiebaud.quiet.utils.AuthCallback;
import com.thomasthiebaud.quiet.utils.Authentication;
import com.thomasthiebaud.quiet.contract.AuthenticationContract;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.utils.Quiet;
import com.thomasthiebaud.quiet.component.DismissSnackbar;
import com.thomasthiebaud.quiet.utils.ErrorHandler;
import com.thomasthiebaud.quiet.utils.Widget;

import java.util.List;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = SignInActivity.class.getSimpleName();

    private Tracker tracker;
    private Button signIn;

    private AuthCallback authResultCallback;
    private Snackbar authSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        tracker = application.getDefaultTracker();

        Dexter.initialize(this);
        Authentication.initialize(this, this, this);

        signIn = (Button) findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(this);

        authSnackbar = Snackbar.make(findViewById(R.id.rootView), getString(R.string.authenticating), Snackbar.LENGTH_INDEFINITE);
        authResultCallback = new AuthCallback() {
            @Override
            public void onSuccess(String idToken) {
                authSnackbar.dismiss();
                checkPermission();
            }

            @Override
            public void onError(int errorCode) {
                authSnackbar.dismiss();
                String message = ErrorHandler.getStandardMessage(getApplicationContext(), errorCode);
                Log.e(TAG, errorCode + "");
                if(errorCode == ErrorHandler.SIGN_IN_REQUIRED)
                    return;

                DismissSnackbar.make(getApplicationContext(), findViewById(R.id.rootView), message).show();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        authSnackbar.show();
        Authentication.getInstance().silentSignIn(authResultCallback);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AuthenticationContract.SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Authentication.getInstance().handleSignInResult(result, authResultCallback);
        }
    }

    private void checkPermission() {
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()) {
                    Context context = getApplicationContext();

                    Quiet.start(context);
                    Widget.idle(context);

                    String title = getResources().getString(R.string.running_app_title);
                    String description = getResources().getString(R.string.running_app_description);
                    DisplayActivity.displaySuccess(context,title, description);
                } else {
                    String description = getResources().getString(R.string.permission_denied_description);
                    DismissSnackbar.make(getApplicationContext(), findViewById(R.id.rootView), description).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS);
    }

    private void signIn() {
        authSnackbar.show();
        Intent signInIntent = Authentication.getInstance().signIn();
        startActivityForResult(signInIntent, AuthenticationContract.SIGN_IN_REQUEST_CODE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        int errorCode = ErrorHandler.handleResultError(connectionResult.getErrorCode());
        String message = ErrorHandler.getStandardMessage(getApplicationContext(), errorCode);
        DismissSnackbar.make(getApplicationContext(), findViewById(R.id.rootView), message).show();
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
