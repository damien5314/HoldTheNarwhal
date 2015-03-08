package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public class RedditPreferences {
    public static final String PREFS_DEVICE_ID = "prefs_device_id";
    public static final String PREF_DEVICE_ID = "pref_device_id";

    public static final String COMMENT_SORT = "pref_comment_sort";
    public static final String LINKS_SORT = "pref_links_sort";
    public static final String LINKS_TIMESPAN = "pref_links_timespan";

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
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(COMMENT_SORT, mContext.getString(R.string.default_comment_sort));
    }

    public void saveCommentSort(String pref) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(COMMENT_SORT, pref)
                .apply();
    }

    public String getLinksSort() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(LINKS_SORT, mContext.getString(R.string.default_links_sort));
    }

    public void saveLinksSort(String pref) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(LINKS_SORT, pref)
                .apply();
    }

    public String getLinksTimespan() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(LINKS_TIMESPAN, mContext.getString(R.string.default_links_timespan));
    }

    public void saveLinksTimespan(String pref) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(LINKS_TIMESPAN, pref)
                .apply();
    }
}
