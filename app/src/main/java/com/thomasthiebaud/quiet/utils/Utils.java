package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.IntentContract;

/**
 * Created by thomasthiebaud on 5/11/16.
 */
public final class Utils {
    private Utils() {}

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

    public static void isRunning(Context context, boolean isRunning) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isRunning", isRunning);
        editor.commit();
    }

    public static boolean isRunning(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isRunning", false);
    }

    public static void setNumber(Context context, String number) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("number", number);
        editor.commit();
    }

    public static String getNumber(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        return sharedPref.getString("number", "");
    }

    public static void removeNumber(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("number");
        editor.commit();
    }

    public static void updateWidget(Context context, int icon, String status) {
        Intent updateWidget = new Intent(IntentContract.WIDGET_ACTION);
        updateWidget.putExtra(IntentContract.ICON, icon);
        updateWidget.putExtra(IntentContract.STATUS, status);

        context.sendBroadcast(updateWidget);
    }
}
