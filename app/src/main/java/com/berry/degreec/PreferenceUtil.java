package com.berry.degreec;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
    public static final String TEMPERATURE = "TEMPERATURE";
    public static final String TIME = "TIME";
    public static final String CONDITION = "CONDITION";
    private static PreferenceUtil instance;
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    public static boolean isValid() {
        return sharedPref != null && editor != null;
    }

    private PreferenceUtil(Context context) {
        sharedPref = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public static void init(Context context) {
        instance = new PreferenceUtil(context);
    }

    public static  void savePreference(String reference, int value) {
        editor.putInt(reference, value);
        editor.apply();
    }

    public static  void savePreference(String reference, float value) {
        editor.putFloat(reference, value);
        editor.apply();
    }

    public static  void savePreference(String reference, String value) {
        editor.putString(reference, value);
        editor.apply();
    }

    public static  int getIntPreference(String reference) {
        return sharedPref.getInt(reference, -1);
    }

    public static  float getFloatPreference(String reference) {
        return sharedPref.getFloat(reference, Float.MIN_VALUE);
    }

    public static  String getStringPreference(String reference) {
        return sharedPref.getString(reference, null);
    }
}
