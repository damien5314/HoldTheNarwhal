package com.ddiehl.android.htn.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.ddiehl.android.htn.R;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.UserSettings;
import timber.log.Timber;

public class SettingsManagerImpl implements SettingsManager {

    public static final String PREFS_USER = "prefs_user";

    // app settings
    public static final String PREFS_DEVICE_ID = "prefs_device_id";
    public static final String PREF_DEVICE_ID = "pref_device_id";
    public static final String PREF_ALLOW_ANALYTICS = "pref_allow_analytics";
    public static final String PREF_ALLOW_ANALYTICS_ASKED = "pref_allow_analytics_asked";
    public static final String PREF_USE_CHROME_TABS = "pref_use_chrome_tabs";
    public static final String PREF_FONT = "pref_font";

    // reddit settings
    public static final String PREF_HAS_FROM_REMOTE = "pref_flag_for_user";
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

    public static final String PREFS_REDDIT = "threaded_messages, hide_downs, email_messages, " +
            "show_link_flair, creddit_autorenew, show_trending, private_feeds, monitor_mentions, " +
            "research, ignore_suggested_sort, media, clickgadget, use_global_defaults, " +
            "label_nsfw, domain_details, show_stylesheets, highlight_controversial, no_profanity, " +
            "default_theme_sr, lang, hide_ups, hide_from_robots, compress, store_visits, beta, " +
            "show_gold_expiration, over_18, enable_default_themes, show_promote, " +
            "min_comment_score, public_votes, organic, collapse_read_messages, show_flair, " +
            "mark_messages_read, hide_ads, min_link_score, newwindow, numsites, num_comments, " +
            "highlight_new_comments, default_comment_sort, hide_locationbar";

    private final Context mContext;
    private final RedditService mRedditService; // FIXME: Bad dependency
    private final SharedPreferences mSharedPreferences;

    private boolean mIsChanging = false;

    public SettingsManagerImpl(Context context, RedditService service) {
        mContext = context;
        mRedditService = service;
        mSharedPreferences = mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private Object getValueFromKey(SharedPreferences sp, String key) {
        return sp.getAll().get(key);
    }

    @Override
    public boolean hasFromRemote() {
        return mSharedPreferences.getBoolean(PREF_HAS_FROM_REMOTE, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (mIsChanging) return;
        mIsChanging = true;

        Map<String, String> changedSettings = new HashMap<>(); // Track changed keys and values

        switch (key) {
            case SettingsManagerImpl.PREF_ALLOW_ANALYTICS:
                boolean allowed = sp.getBoolean(PREF_ALLOW_ANALYTICS, false);
                break;
            default:
                Object p = getValueFromKey(sp, key);
                changedSettings.put(key, String.valueOf(p));
                break;
        }

        // Force "make safe(r) for work" to be true if "over 18" is false
        boolean over18 = sp.getBoolean(SettingsManagerImpl.PREF_OVER_18, false);
        if (!over18) {
            boolean noProfanity = sp.getBoolean(SettingsManagerImpl.PREF_NO_PROFANITY, true);
            if (!noProfanity) {
                sp.edit().putBoolean(SettingsManagerImpl.PREF_NO_PROFANITY, true).apply();
                changedSettings.put(SettingsManagerImpl.PREF_NO_PROFANITY, String.valueOf(true));
            }
        }

        // Force "label nsfw" to be true if "make safe(r) for work" is true
        boolean noProfanity = sp.getBoolean(SettingsManagerImpl.PREF_NO_PROFANITY, true);
        if (noProfanity) {
            boolean labelNsfw = sp.getBoolean(SettingsManagerImpl.PREF_LABEL_NSFW, true);
            if (!labelNsfw) {
                sp.edit().putBoolean(SettingsManagerImpl.PREF_LABEL_NSFW, true).apply();
                changedSettings.put(SettingsManagerImpl.PREF_LABEL_NSFW, String.valueOf(true));
            }
        }

        if (changedSettings.size() > 0 && mRedditService.isUserAuthorized()) {
            // Post SettingsUpdate event with changed keys and values
            mRedditService.updateUserSettings(changedSettings)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> Timber.d("Settings updated successfully"),
                            error -> Timber.w(error, "Error updating settings")
                    );
        }

        Map prefs = sp.getAll();

        mIsChanging = false;
    }

    @Override
    public void saveUserSettings(UserSettings settings) {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mSharedPreferences.edit()
                .putBoolean(PREF_HAS_FROM_REMOTE, true)
                .putBoolean(PREF_BETA, settings.getBeta())
                .putBoolean(PREF_CLICKGAGDET, settings.getClickgadget())
                .putBoolean(PREF_COLLAPSE_READ_MESSAGES, settings.getCollapseReadMessages())
                .putBoolean(PREF_COMPRESS, settings.getCompress())
                .putBoolean(PREF_CREDDIT_AUTORENEW, settings.getCredditAutoRenew())
                .putString(PREF_DEFAULT_COMMENT_SORT, settings.getDefaultCommentSort())
                .putBoolean(PREF_DOMAIN_DETAILS, settings.getDomainDetails())
                .putBoolean(PREF_EMAIL_MESSAGES, settings.getEmailMessages())
                .putBoolean(PREF_ENABLE_DEFAULT_THEMES, settings.getEnableDefaultThemes())
                .putBoolean(PREF_HIDE_ADS, settings.getHideAds())
                .putBoolean(PREF_HIDE_DOWNS, settings.getHideDowns())
                .putBoolean(PREF_HIDE_FROM_ROBOTS, settings.getHideFromRobots())
                .putBoolean(PREF_HIDE_LOCATIONBAR, settings.getHideLocationBar())
                .putBoolean(PREF_HIDE_UPS, settings.getHideUps())
                .putBoolean(PREF_HIGHLIGHT_CONTROVERSIAL, settings.getHighlightControversial())
                .putBoolean(PREF_HIGHLIGHT_NEW_COMMENTS, settings.getHighlightNewComments())
                .putBoolean(PREF_IGNORE_SUGGESTED_SORT, settings.getIgnoreSuggestedSort())
                .putBoolean(PREF_LABEL_NSFW, settings.getLabelNsfw())
                .putString(PREF_LANG, settings.getLang())
                .putBoolean(PREF_MARK_MESSAGES_READ, settings.getMarkMessagesRead())
                .putString(PREF_MEDIA, settings.getMedia())
                .putString(PREF_MIN_COMMENT_SCORE, String.valueOf(settings.getMinCommentScore()).equals("null")
                        ? "" : String.valueOf(settings.getMinCommentScore()))
                .putString(PREF_MIN_LINK_SCORE, String.valueOf(settings.getMinLinkScore()).equals("null")
                        ? "" : String.valueOf(settings.getMinLinkScore()))
                .putBoolean(PREF_MONITOR_MENTIONS, settings.getMonitorMentions())
                .putBoolean(PREF_NEWWINDOW, settings.getNewWindow())
                .putBoolean(PREF_NO_PROFANITY, settings.getNoProfanity())
                .putString(PREF_NUM_COMMENTS, String.valueOf(settings.getNumComments()))
                .putString(PREF_NUMSITES, String.valueOf(settings.getNumLinks()))
                .putBoolean(PREF_ORGANIC, settings.getOrganic())
                .putBoolean(PREF_OVER_18, settings.getOver18())
                .putBoolean(PREF_PUBLIC_FEEDS, settings.getPublicFeeds())
                .putBoolean(PREF_PUBLIC_VOTES, settings.getPublicVotes())
                .putBoolean(PREF_RESEARCH, settings.getResearch())
                .putBoolean(PREF_SHOW_FLAIR, settings.getShowFlair())
                .putBoolean(PREF_SHOW_GOLD_EXPIRATION, settings.getShowGoldExpiration())
                .putBoolean(PREF_SHOW_LINK_FLAIR, settings.getShowLinkFlair())
                .putBoolean(PREF_SHOW_PROMOTE, settings.getShowPromote())
                .putBoolean(PREF_SHOW_STYLESHEETS, settings.getShowStylesheets())
                .putBoolean(PREF_SHOW_TRENDING, settings.getShowTrending())
                .putBoolean(PREF_STORE_VISITS, settings.getStoreVisits())
                .putString(PREF_THEME_SELECTOR, settings.getThemeSelector())
                .putBoolean(PREF_THREADED_MESSAGES, settings.getThreadedMessages())
                .putBoolean(PREF_USE_GLOBAL_DEFAULTS, settings.getUseGlobalDefaults())
                .apply();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void clearUserSettings() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        // Only removing reddit preferences, app preferences can stay the same
        // Need to do this because PreferenceFragment can only show preferences from one SP instance
        mSharedPreferences.edit()
                .remove(PREF_HAS_FROM_REMOTE)
                .remove(PREF_BETA)
                .remove(PREF_CLICKGAGDET)
                .remove(PREF_COLLAPSE_READ_MESSAGES)
                .remove(PREF_COMPRESS)
                .remove(PREF_CREDDIT_AUTORENEW)
                .remove(PREF_DEFAULT_COMMENT_SORT)
                .remove(PREF_DOMAIN_DETAILS)
                .remove(PREF_EMAIL_MESSAGES)
                .remove(PREF_ENABLE_DEFAULT_THEMES)
                .remove(PREF_HIDE_ADS)
                .remove(PREF_HIDE_DOWNS)
                .remove(PREF_HIDE_FROM_ROBOTS)
                .remove(PREF_HIDE_LOCATIONBAR)
                .remove(PREF_HIDE_UPS)
                .remove(PREF_HIGHLIGHT_CONTROVERSIAL)
                .remove(PREF_HIGHLIGHT_NEW_COMMENTS)
                .remove(PREF_IGNORE_SUGGESTED_SORT)
                .remove(PREF_LABEL_NSFW)
                .remove(PREF_LANG)
                .remove(PREF_MARK_MESSAGES_READ)
                .remove(PREF_MEDIA)
                .remove(PREF_MIN_COMMENT_SCORE)
                .remove(PREF_MIN_LINK_SCORE)
                .remove(PREF_MONITOR_MENTIONS)
                .remove(PREF_NEWWINDOW)
                .remove(PREF_NO_PROFANITY)
                .remove(PREF_NUM_COMMENTS)
                .remove(PREF_NUMSITES)
                .remove(PREF_ORGANIC)
                .remove(PREF_OVER_18)
                .remove(PREF_PUBLIC_FEEDS)
                .remove(PREF_PUBLIC_VOTES)
                .remove(PREF_RESEARCH)
                .remove(PREF_SHOW_FLAIR)
                .remove(PREF_SHOW_GOLD_EXPIRATION)
                .remove(PREF_SHOW_LINK_FLAIR)
                .remove(PREF_SHOW_PROMOTE)
                .remove(PREF_SHOW_STYLESHEETS)
                .remove(PREF_SHOW_TRENDING)
                .remove(PREF_STORE_VISITS)
                .remove(PREF_THEME_SELECTOR)
                .remove(PREF_THREADED_MESSAGES)
                .remove(PREF_USE_GLOBAL_DEFAULTS)
                .apply();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    //////////////////
    // App settings //
    //////////////////

    @Override
    public String getDeviceId() {
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_DEVICE_ID, Context.MODE_PRIVATE);
        String deviceId = sp.getString(PREF_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            sp.edit().putString(PREF_DEVICE_ID, deviceId).apply();
        }
        return deviceId;
    }

    @Override
    public boolean areAnalyticsEnabled() {
        return mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .getBoolean(PREF_ALLOW_ANALYTICS, false);
    }

    @Override
    public void setAnalyticsEnabled(boolean b) {
        mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE).edit()
                .putBoolean(PREF_ALLOW_ANALYTICS, b)
                .apply();
    }

    @Override
    public boolean askedForAnalytics() {
        return mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
                .getBoolean(PREF_ALLOW_ANALYTICS_ASKED, false);
    }

    @Override
    public void setAskedForAnalytics(boolean b) {
        mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE).edit()
                .putBoolean(PREF_ALLOW_ANALYTICS_ASKED, b)
                .apply();
    }

    @Override
    public boolean customTabsEnabled() {
        return mSharedPreferences.getBoolean(PREF_USE_CHROME_TABS, true);
    }

    @Override
    public String getFont() {
        return mSharedPreferences.getString(PREF_FONT, "");
    }

    /////////////////////
    // Reddit settings //
    /////////////////////

    @Override
    public String getCommentSort() {
        return mSharedPreferences.getString(PREF_DEFAULT_COMMENT_SORT,
                mContext.getString(R.string.default_comment_sort));
    }

    @Override
    public void saveCommentSort(String pref) {
        mSharedPreferences.edit()
                .putString(PREF_DEFAULT_COMMENT_SORT, pref)
                .apply();
    }

    @Override
    public Integer getMinCommentScore() {
        String str = mSharedPreferences.getString(PREF_MIN_COMMENT_SCORE, null);
        if (str == null) {
            return null;
        } else {
            try {
                return Integer.valueOf(str);
            } catch (Exception e) {
                // In all likelihood, this isn't an integer ("null")
                return null;
            }
        }
    }

    @Override
    public boolean getShowControversiality() {
        return mSharedPreferences.getBoolean(PREF_HIGHLIGHT_CONTROVERSIAL, false);
    }

    @Override
    public boolean getOver18() {
        return mSharedPreferences.getBoolean(PREF_OVER_18, false);
    }

    @Override
    public void setOver18(boolean b) {
        mSharedPreferences.edit()
                .putBoolean(PREF_OVER_18, b)
                .apply();
    }

    @Override
    public boolean getNoProfanity() {
        return mSharedPreferences.getBoolean(PREF_NO_PROFANITY, true);
    }

    @Override
    public boolean getLabelNsfw() {
        return mSharedPreferences.getBoolean(PREF_LABEL_NSFW, true);
    }
}
