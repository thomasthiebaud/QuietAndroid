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
import com.thomasthiebaud.quiet.contract.ErrorContract;
import com.thomasthiebaud.quiet.utils.AuthCallback;
import com.thomasthiebaud.quiet.utils.AuthUtils;
import com.thomasthiebaud.quiet.contract.SignInContract;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.utils.Utils;

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
        AuthUtils.initialize(this, this, this);

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
                String message = "";
                boolean hide = false;
                switch (errorCode) {
                    case ErrorContract.UNKNOWN_ERROR:
                        message = getString(R.string.unknown_error);
                        break;
                    case ErrorContract.CONNECTION_ERROR:
                        message = getString(R.string.connection_failed);
                        break;
                    case ErrorContract.SIGN_IN_REQUIRED:
                        hide = true;
                        break;
                }

                if(hide)
                    return;

                final Snackbar snackBar = Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction(getString(R.string.dismiss), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        authSnackbar.show();
        AuthUtils.getInstance().silentSignIn(authResultCallback);
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

        if (requestCode == SignInContract.SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            AuthUtils.getInstance().handleSignInResult(result, authResultCallback);
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
                    final Snackbar snackBar = Snackbar.make(findViewById(R.id.rootView), description, Snackbar.LENGTH_INDEFINITE);
                    snackBar.setAction(getString(R.string.dismiss), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackBar.dismiss();
                        }
                    });
                    snackBar.show();
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
        Intent signInIntent = AuthUtils.getInstance().signIn();
        startActivityForResult(signInIntent, SignInContract.SIGN_IN_REQUEST_CODE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        final Snackbar snackBar = Snackbar.make(findViewById(R.id.rootView), getString(R.string.connection_failed) + " WAZAA", Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(getString(R.string.dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
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
