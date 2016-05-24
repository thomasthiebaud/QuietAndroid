package com.thomasthiebaud.quiet.utils;

/**
 * Created by thomasthiebaud on 5/23/16.
 */
public interface AuthCallback {
    void onSuccess(String idToken);

    void onError(int code);
}
