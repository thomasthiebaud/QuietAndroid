package com.thomasthiebaud.quiet.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
}
