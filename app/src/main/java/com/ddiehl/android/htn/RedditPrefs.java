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

    // app settings
    public static final String PREF_ENABLE_ADS = "pref_enable_ads";

    // reddit settings
    public static final String PREF_BETA = "beta";
    public static final String PREF_CLICKGAGDET = "clickgadget";
    public static final String PREF_COLLAPSE_READ_MESSAGES = "collapse_read_messages";
    public static final String PREF_COMPRESS = "compress";
    public static final String PREF_CREDDIT_AUTORENEW = "creddit_autorenew";
    public static final String PREF_DEFAULT_COMMENT_SORT = "default_comment_sort";
    public static final String PREF_DOMAIN_DETAILS = "domain_details";
    public static final String PREF_EMAIL_MESSAGES = "email_messages";
    public static final String PREF_ENABLE_DEFAULT_THEMES = "enable_default_themes";
    public static final String PREF_HIDE_ADS = "hide_ads";
    public static final String PREF_HIDE_DOWNS = "hide_downs";
    public static final String PREF_HIDE_FROM_ROBOTS = "hide_from_robots";
    public static final String PREF_HIDE_LOCATIONBAR = "hide_locationbar";
    public static final String PREF_HIDE_UPS = "hide_ups";
    public static final String PREF_HIGHLIGHT_CONTROVERSIAL = "highlight_controversial";
    public static final String PREF_HIGHLIGHT_NEW_COMMENTS = "highlight_new_comments";
    public static final String PREF_IGNORE_SUGGESTED_SORT = "ignore_suggested_sort";
    public static final String PREF_LABEL_NSFW = "label_nsfw";
    public static final String PREF_LANG = "lang";
    public static final String PREF_MARK_MESSAGES_READ = "mark_messages_read";
    public static final String PREF_MEDIA = "media";
    public static final String PREF_MIN_COMMENT_SCORE = "min_comment_score";
    public static final String PREF_MIN_LINK_SCORE = "min_link_score";
    public static final String PREF_MONITOR_MENTIONS = "monitor_mentions";
    public static final String PREF_NEWWINDOW = "newwindow";
    public static final String PREF_NO_PROFANITY = "no_profanity";
    public static final String PREF_NUM_COMMENTS = "num_comments";
    public static final String PREF_NUMSITES = "numsites";
    public static final String PREF_ORGANIC = "organic";
    public static final String PREF_OVER_18 = "over_18";
    public static final String PREF_PUBLIC_FEEDS = "public_feeds";
    public static final String PREF_PUBLIC_VOTES = "public_votes";
    public static final String PREF_RESEARCH = "research";
    public static final String PREF_SHOW_FLAIR = "show_flair";
    public static final String PREF_SHOW_GOLD_EXPIRATION = "show_gold_expiration";
    public static final String PREF_SHOW_LINK_FLAIR = "show_link_flair";
    public static final String PREF_SHOW_PROMOTE = "show_promote";
    public static final String PREF_SHOW_STYLESHEETS = "show_stylesheets";
    public static final String PREF_SHOW_TRENDING = "show_trending";
    public static final String PREF_STORE_VISITS = "store_visits";
    public static final String PREF_THEME_SELECTOR = "theme_selector";
    public static final String PREF_THREADED_MESSAGES = "threaded_messages";
    public static final String PREF_USE_GLOBAL_DEFAULTS = "use_global_defaults";

    public static final String PREFS_REDDIT = "threaded_messages, hide_downs, email_messages, show_link_flair, " +
            "creddit_autorenew, show_trending, private_feeds, monitor_mentions, research, ignore_suggested_sort, " +
            "media, clickgadget, use_global_defaults, label_nsfw, domain_details, show_stylesheets, " +
            "highlight_controversial, no_profanity, default_theme_sr, lang, hide_ups, hide_from_robots, " +
            "compress, store_visits, beta, show_gold_expiration, over_18, enable_default_themes, show_promote, " +
            "min_comment_score, public_votes, organic, collapse_read_messages, show_flair, mark_messages_read, " +
            "hide_ads, min_link_score, newwindow, numsites, num_comments, highlight_new_comments, " +
            "default_comment_sort, hide_locationbar";

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

    public String getCommentSort() {
        return mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .getString(PREF_DEFAULT_COMMENT_SORT, mContext.getString(R.string.default_comment_sort));
    }

    public void saveCommentSort(String pref) {
        mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_DEFAULT_COMMENT_SORT, pref)
                .apply();
    }

    public boolean getAdsEnabled() {
        SharedPreferences sp = mContext.getSharedPreferences(RedditPrefs.PREFS_USER, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_ENABLE_ADS, false);
    }
}
