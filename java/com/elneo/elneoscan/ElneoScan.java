package com.elneo.elneoscan;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;

public class ElneoScan extends Application {
    public static final String LOG_TAG          = "##ElneoScan##";
    public static final String USER_LOGIN       = "user_login";
    public static final String USER_PASSWORD    = "user_pwd";
    public static final String USER_UID         = "user_uid";

    public static final String KEY_PREF_SELECTED_WAREHOUSE = "warehouse_selected";
    public static final String KEY_PREF_AISLE = "aisle";

    public static String getUserLogin(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = "";

        try {
            String value = sp.getString(USER_LOGIN, defaultValue);
            return value;
        } catch(Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static String getUserPassword(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = "";

        try {
            String value = sp.getString(USER_PASSWORD, defaultValue);
            return value;
        } catch(Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static int getUserUid(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultValue = -1;

        try {
            return sp.getInt(USER_UID, defaultValue);
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean hasCredentials(Context context) {
        return (ElneoScan.getUserLogin(context) != ""
                && ElneoScan.getUserPassword(context) != ""
                && ElneoScan.getUserUid(context) != -1) ? true : false;
    }

    public static String getUserWarehouse(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = "";

        try {
            String value = sp.getString(KEY_PREF_SELECTED_WAREHOUSE, defaultValue);
            return value;
        } catch(Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static String getUserAisle(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = "";

        try {
            String value = sp.getString(KEY_PREF_AISLE, defaultValue);
            return value;
        } catch(Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}