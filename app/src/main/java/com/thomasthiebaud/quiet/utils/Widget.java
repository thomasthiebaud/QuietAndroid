package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.IntentContract;

/**
 * Created by thomasthiebaud on 5/28/16.
 */
public class Widget {
    public static void idle(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("number");
        editor.commit();

        Intent updateWidget = new Intent(IntentContract.WIDGET_ACTION);
        updateWidget.putExtra(IntentContract.ICON, R.drawable.running);
        updateWidget.putExtra(IntentContract.STATUS, context.getString(R.string.quiet_is_running));

        context.sendBroadcast(updateWidget);
    }

    public static void safe(Context context) {
        Intent updateWidget = new Intent(IntentContract.WIDGET_ACTION);
        updateWidget.putExtra(IntentContract.ICON, R.drawable.safe);
        updateWidget.putExtra(IntentContract.STATUS, context.getString(R.string.safe));

        context.sendBroadcast(updateWidget);
    }

    public static void ringing(Context context, String number, int icon, String description) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("number", number);
        editor.commit();

        Intent updateWidget = new Intent(IntentContract.WIDGET_ACTION);
        updateWidget.putExtra(IntentContract.ICON, icon);
        updateWidget.putExtra(IntentContract.STATUS, description);

        context.sendBroadcast(updateWidget);
    }
}
