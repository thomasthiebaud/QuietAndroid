package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.thomasthiebaud.quiet.R;

/**
 * Created by thomasthiebaud on 5/25/16.
 */
public class DismissSnackbar {
    public static Snackbar make(Context context, View view, String message) {
        final Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(context.getString(R.string.dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        return snackBar;
    }
}
