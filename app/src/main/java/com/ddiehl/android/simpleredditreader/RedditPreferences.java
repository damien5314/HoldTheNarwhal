package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.preference.PreferenceManager;

public class RedditPreferences {
    public static final String COMMENT_SORT = "pref_comment_sort";

    public static String getCommentSort(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString(RedditPreferences.COMMENT_SORT, c.getString(R.string.default_comment_sort));
    }

    public static void saveCommentSort(Context c, String pref) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(COMMENT_SORT, pref)
                .apply();
    }
}
