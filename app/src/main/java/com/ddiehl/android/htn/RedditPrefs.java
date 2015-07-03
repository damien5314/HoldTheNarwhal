/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class RedditPrefs {
    public static final String PREFS_DEVICE_ID = "prefs_device_id";
    private static final String PREF_DEVICE_ID = "pref_device_id";

    public static final String PREFS_USER = "prefs_user";

    // app settings
    private static final String PREF_ENABLE_ADS = "pref_enable_ads";

    // reddit settings
    private static final String PREF_THREADED_MESSAGES = "pref_threaded_messages";
    private static final String PREF_HIDE_DOWNS = "pref_hide_downs";
    private static final String PREF_EMAIL_MESSAGES = "pref_email_messages";
    private static final String PREF_SHOW_LINK_FLAIR = "pref_show_link_flair";
    private static final String PREF_CREDDIT_AUTORENEW = "pref_creddit_autorenew";
    private static final String PREF_SHOW_TRENDING = "pref_show_trending";
    private static final String PREF_PRIVATE_FEEDS = "pref_private_feeds";
    private static final String PREF_MONITOR_MENTIONS = "pref_monitor_mentions";
    private static final String PREF_RESEARCH = "pref_research";
    private static final String PREF_IGNORE_SUGGESTED_SORT = "pref_ignore_suggested_sort";
    private static final String PREF_MEDIA = "pref_media";
    private static final String PREF_CLICKGAGDET = "pref_clickgagdet";
    private static final String PREF_USE_GLOBAL_DEFAULTS = "pref_use_global_defaults";
    private static final String PREF_LABEL_NSFW = "pref_label_nsfw";
    private static final String PREF_DOMAIN_DETAILS = "pref_domain_details";
    private static final String PREF_SHOW_STYLESHEETS = "pref_show_stylesheets";
    private static final String PREF_HIGHLIGHT_CONTROVERSIAL = "pref_highlight_controversial";
    private static final String PREF_NO_PROFANITY = "pref_no_profanity";
    private static final String PREF_DEFAULT_THEME_SR = "pref_default_theme_sr";
    private static final String PREF_LANG = "pref_lang";
    private static final String PREF_HIDE_UPS = "pref_hide_ups";
    private static final String PREF_HIDE_FROM_ROBOTS = "pref_hide_from_robots";
    private static final String PREF_COMPRESS = "pref_compress";
    private static final String PREF_STORE_VISITS = "pref_store_visits";
    private static final String PREF_BETA = "pref_beta";
    private static final String PREF_SHOW_GOLD_EXPIRATION = "pref_show_gold_expiration";
    private static final String PREF_OVER_18 = "pref_over_18";
    private static final String PREF_ENABLE_DEFAULT_THEMES = "pref_enable_default_themes";
    private static final String PREF_SHOW_PROMOTE = "pref_show_promote";
    private static final String PREF_MIN_COMMENT_SCORE = "pref_min_comment_score";
    private static final String PREF_PUBLIC_VOTES = "pref_public_votes";
    private static final String PREF_ORGANIC = "pref_organic";
    private static final String PREF_COLLAPSE_READ_MESSAGES = "pref_collapse_read_messages";
    private static final String PREF_SHOW_FLAIR = "pref_show_flair";
    private static final String PREF_MARK_MESSAGES_READ = "pref_mark_messages_read";
    private static final String PREF_HIDE_ADS = "pref_hide_ads";
    private static final String PREF_MIN_LINK_SCORE = "pref_min_link_score";
    private static final String PREF_NEWWINDOW = "pref_newwindow";
    private static final String PREF_NUMSITES = "pref_numsites";
    private static final String PREF_NUM_COMMENTS = "pref_num_comments";
    private static final String PREF_HIGHLIGHT_NEW_COMMENTS = "pref_highlight_new_comments";
    private static final String PREF_DEFAULT_COMMENT_SORT = "pref_default_comment_sort";
    private static final String PREF_HIDE_LOCATIONBAR = "pref_hide_locationbar";

    public static final String PREFS_REDDIT =
            "threaded_messages, hide_downs, email_messages, show_link_flair, creddit_autorenew, " +
                    "show_trending, private_feeds, monitor_mentions, research, ignore_suggested_sort, " +
                    "media, clickgadget, use_global_defaults, label_nsfw, domain_details, " +
                    "show_stylesheets, highlight_controversial, no_profanity, default_theme_sr, " +
                    "lang, hide_ups, hide_from_robots, compress, store_visits, beta, " +
                    "show_gold_expiration, over_18, enable_default_themes, show_promote, " +
                    "min_comment_score, public_votes, organic, collapse_read_messages, show_flair, " +
                    "mark_messages_read, hide_ads, min_link_score, newwindow, numsites, num_comments, " +
                    "highlight_new_comments, default_comment_sort, hide_locationbar";

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
