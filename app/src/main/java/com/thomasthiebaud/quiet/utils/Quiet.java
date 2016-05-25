package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.IntentContract;

/**
 * Created by thomasthiebaud on 5/11/16.
 */
public final class Quiet {
    private Quiet() {}

    public static void start(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isRunning", true);
        editor.commit();
    }

    public static void stop(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isRunning", false);
        editor.commit();
    }

    public static boolean isRunning(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("QuietPref", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isRunning", false);
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
}
