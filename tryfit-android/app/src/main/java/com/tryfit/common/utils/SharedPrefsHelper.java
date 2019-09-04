package com.tryfit.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by alexeyreznik on 18/08/2017.
 */

public class SharedPrefsHelper {

    public static final String SP_TAG = "shared_prefs";
    public static final String SP_DEBUG = "DEBUG";
    public static final String SP_PAPER_SIZE = "PAPER_SIZE";
    public static final String SP_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SP_EMULATE_SCANNER = "EMULATE_SCANNER";


    public static void putString(Context context, String tag, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SP_TAG,
                Context.MODE_PRIVATE).edit();
        editor.putString(tag, value);
        editor.apply();
    }

    public static String getString(Context context, String tag, String defaultValue) {
        return context.getSharedPreferences(SP_TAG,
                Context.MODE_PRIVATE).getString(tag, defaultValue);
    }

    public static void putBoolean(Context context, String tag, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SP_TAG,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(tag, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String tag, boolean defaultValue) {
        return context.getSharedPreferences(SP_TAG,
                Context.MODE_PRIVATE).getBoolean(tag, defaultValue);
    }
}
