/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class RedditPrefs {
    public static final String PREFS_DEVICE_ID = "prefs_device_id";
    public static final String PREF_DEVICE_ID = "pref_device_id";

    public static final String PREFS_USER = "prefs_user";
    public static final String PREF_ALLOW_ANALYTICS = "pref_allow_analytics";

    private static RedditPrefs _instance;
    private Context mContext;

    private RedditPrefs(Context context) {
        mContext = context.getApplicationContext();
    }

    public static RedditPrefs getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditPrefs.class) {
                if (_instance == null) {
                    _instance = new RedditPrefs(context);
                }
            }
        }
        return _instance;
    }

    public String getDeviceId() {
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_DEVICE_ID, Context.MODE_PRIVATE);
        String deviceId = sp.getString(PREF_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = generateDeviceId();
        }
        return deviceId;
    }

    private String generateDeviceId() {
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_DEVICE_ID, Context.MODE_PRIVATE);
        String deviceId = UUID.randomUUID().toString();
        sp.edit().putString(PREF_DEVICE_ID, deviceId).apply();
        return deviceId;
    }

    public static boolean areAnalyticsEnabled(Context c) {
        return c.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .getBoolean(PREF_ALLOW_ANALYTICS, false);
    }
}
