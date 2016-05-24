package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.thomasthiebaud.quiet.BuildConfig;
import com.thomasthiebaud.quiet.contract.ErrorContract;
import com.thomasthiebaud.quiet.contract.SignInContract;
import com.thomasthiebaud.quiet.model.Body;
import com.thomasthiebaud.quiet.model.Message;
import com.thomasthiebaud.quiet.services.HttpService;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by thomasthiebaud on 5/23/16.
 */
public class AuthUtils {
    private static final String TAG = AuthUtils.class.getSimpleName();
    private static AuthUtils instance = null;
    private GoogleApiClient googleApiClient;
    private Context context;

    public static AuthUtils initialize(Context context, FragmentActivity fragmentActivity, GoogleApiClient.OnConnectionFailedListener unresolvedConnectionFailedListener) {
        if(AuthUtils.instance == null)
            AuthUtils.instance = new AuthUtils(context, fragmentActivity, unresolvedConnectionFailedListener);
        return AuthUtils.instance;
    }

    public static AuthUtils getInstance() {
        return AuthUtils.instance;
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

            Body body = new Body().add(SignInContract.ID_TOKEN, acct.getIdToken());

            Call<Message> results = HttpService.getInstance().getQuietApi().signIn(body);
            results.enqueue(new Callback<Message>() {
                @Override
                public void onResponse(Call<Message> call, Response<Message> response) {
                    Utils.saveIdToken(context, acct.getIdToken());
                    callback.onSuccess(acct.getIdToken());
                }

                @Override
                public void onFailure(Call<Message> call, Throwable t) {
                    Log.e(TAG + "#onFailure", t.getMessage());

                    int code = ErrorContract.UNKNOWN_ERROR;

                    if(t instanceof SocketTimeoutException)
                        code = ErrorContract.CONNECTION_ERROR;

                    callback.onError(code);
                }
            });
        } else {
            if(result.getStatus().getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED)
                callback.onError(ErrorContract.SIGN_IN_REQUIRED);
            else
                callback.onError(ErrorContract.ERROR);
        }
    }

    private AuthUtils(Context context, FragmentActivity fragmentActivity, GoogleApiClient.OnConnectionFailedListener unresolvedConnectionFailedListener) {
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
}
