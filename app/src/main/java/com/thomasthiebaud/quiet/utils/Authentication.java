package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.thomasthiebaud.quiet.BuildConfig;
import com.thomasthiebaud.quiet.contract.AuthenticationContract;
import com.thomasthiebaud.quiet.model.Body;
import com.thomasthiebaud.quiet.model.Message;
import com.thomasthiebaud.quiet.services.HttpService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by thomasthiebaud on 5/23/16.
 */
public class Authentication {
    private static final String TAG = Authentication.class.getSimpleName();
    private static Authentication instance = null;
    private GoogleApiClient googleApiClient;
    private Context context;

    public static Authentication initialize(Context context) {
        Authentication.instance = new Authentication(context);
        return Authentication.instance;
    }

    public static Authentication initialize(Context context, FragmentActivity fragmentActivity, GoogleApiClient.OnConnectionFailedListener unresolvedConnectionFailedListener) {
        Authentication.instance = new Authentication(context, fragmentActivity, unresolvedConnectionFailedListener);
        return Authentication.instance;
    }

    public static Authentication getInstance() {
        return Authentication.instance;
    }

    public static void saveIdToken(Context context, String idToken) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("idToken", idToken);
        editor.commit();
    }

    public static String getIdToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        return sharedPref.getString("idToken", "");
    }

    public Intent signIn() {
        return Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    }

    public void silentSignIn(final AuthCallback callback) {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result, callback);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult, callback);
                }
            });
        }
    }

    public void handleSignInResult(GoogleSignInResult result, final AuthCallback callback) {
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();

            Body body = new Body().add(AuthenticationContract.ID_TOKEN, acct.getIdToken());

            Call<Message> results = HttpService.getInstance().getQuietApi().signIn(body);
            results.enqueue(new Callback<Message>() {
                @Override
                public void onResponse(Call<Message> call, Response<Message> response) {
                    if(ErrorHandler.handleQuietError(response.code())) {
                        Log.e(TAG, "handleSignInResult#onResponse : " + response.body().getMessage());
                        callback.onError(response.code());
                    } else {
                        Authentication.saveIdToken(context, acct.getIdToken());
                        callback.onSuccess(acct.getIdToken());
                    }
                }

                @Override
                public void onFailure(Call<Message> call, Throwable t) {
                    Log.e(TAG + "#onFailure", t.getMessage());
                    callback.onError(ErrorHandler.handleRetrofitError(t));
                }
            });
        } else {
            callback.onError(ErrorHandler.handleResultError(result.getStatus().getStatusCode()));
        }
    }

    private Authentication(Context context, FragmentActivity fragmentActivity, GoogleApiClient.OnConnectionFailedListener unresolvedConnectionFailedListener) {
        this.context = context;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.QUIET_SERVER_ID)
                .requestEmail()
                .build();
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(fragmentActivity, unresolvedConnectionFailedListener)
                .addApi(com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private Authentication(Context context) {
        this.context = context;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.QUIET_SERVER_ID)
                .requestEmail()
                .build();
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
}
