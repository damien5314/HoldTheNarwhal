package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.preference.PreferenceManager;

public class RedditPreferences {
    public static final String COMMENT_SORT = "pref_comment_sort";
    public static final String LINKS_SORT = "pref_links_sort";
    public static final String LINKS_TIMESPAN = "pref_links_timespan";

    public static String getCommentSort(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString(COMMENT_SORT, c.getString(R.string.default_comment_sort));
    }

    public static void saveCommentSort(Context c, String pref) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(COMMENT_SORT, pref)
                .apply();
    }

    public static String getLinksSort(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString(LINKS_SORT, c.getString(R.string.default_links_sort));
    }

    public static void saveLinksSort(Context c, String pref) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(LINKS_SORT, pref)
                .apply();
    }

    public static String getLinksTimespan(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString(LINKS_TIMESPAN, c.getString(R.string.default_links_timespan));
    }

    public static void saveLinksTimespan(Context c, String pref) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(LINKS_TIMESPAN, pref)
                .apply();
    }
}
