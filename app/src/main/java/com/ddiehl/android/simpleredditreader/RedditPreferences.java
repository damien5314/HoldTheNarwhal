package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class RedditPreferences {
    public static final String PREFS_DEVICE_ID = "prefs_device_id";
    public static final String PREF_DEVICE_ID = "pref_device_id";

    public static final String PREFS_USER = "prefs_user";
    public static final String PREF_COMMENT_SORT = "pref_comment_sort";

    private static RedditPreferences _instance;
    private Context mContext;

    private RedditPreferences(Context context) {
        mContext = context.getApplicationContext();
    }

    public static RedditPreferences getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditPreferences.class) {
                if (_instance == null) {
                    _instance = new RedditPreferences(context);
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

    public String getCommentSort() {
        return mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .getString(PREF_COMMENT_SORT, mContext.getString(R.string.default_comment_sort));
    }

    public void saveCommentSort(String pref) {
        mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_COMMENT_SORT, pref)
                .apply();
    }
}
