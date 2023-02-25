package com.driver.drowsers.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.driver.drowsers.BAlert;

public class AlertPref {


    public static String CURRENT_TUNE = "CURRENT_TONE";

    private static SharedPreferences preferences;


    private static void init() {
        if (preferences == null)
            preferences = BAlert.getContext().getSharedPreferences("B_ALERT_DATA", Context.MODE_PRIVATE);
    }

    public static void putValue(String key, String value) {
        init();
        preferences.edit().putString(key, value).apply();
    }

    public static String getValue(String key, String defaultValue) {
        init();
        return preferences.getString(key, defaultValue);
    }
}
