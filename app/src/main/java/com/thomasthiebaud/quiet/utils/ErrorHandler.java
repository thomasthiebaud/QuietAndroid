package com.thomasthiebaud.quiet.utils;

import android.content.Context;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.thomasthiebaud.quiet.R;

import java.net.SocketTimeoutException;

/**
 * Created by thomasthiebaud on 5/25/16.
 */
public class ErrorHandler {
    public static final int UNKNOWN_ERROR = 0;
    public static final int NETWORK_ERROR = 1;

    public static final int SIGN_IN_REQUIRED = 2;

    public static int handleRetrofitError(Throwable t) {
        int code = UNKNOWN_ERROR;
        if (t instanceof SocketTimeoutException)
            code = NETWORK_ERROR;
        return code;
    }

    public static int handleResultError(int statusCode) {
        int code = UNKNOWN_ERROR;

        if (statusCode == CommonStatusCodes.SIGN_IN_REQUIRED) {
            code = SIGN_IN_REQUIRED;
        }

        return code;
    }

    public static String getStandardMessage(Context context, int code) {
        switch (code) {
            case NETWORK_ERROR:
                return context.getString(R.string.connection_failed);
            default:
                return context.getString(R.string.unknown_error);
        }
    }
}