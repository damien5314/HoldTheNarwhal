package com.ddiehl.android.htn.settings

import android.content.Context
import android.content.SharedPreferences
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.view.theme.ColorScheme
import com.ddiehl.android.htn.view.theme.ColorScheme.Companion.fromId
import rxreddit.model.UserSettings
import java.util.*

class SettingsManagerImpl(context: Context) : SettingsManager {

    private val appContext: Context = context.applicationContext
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)

    override fun hasFromRemote(): Boolean {
        return sharedPreferences.getBoolean(PREF_HAS_FROM_REMOTE, false)
    }

    override fun saveUserSettings(settings: UserSettings) {
        sharedPreferences.edit()
            .putBoolean(PREF_HAS_FROM_REMOTE, true)
            .putBoolean(PREF_BETA, settings.beta ?: false)
            .putBoolean(PREF_CLICKGAGDET, settings.clickgadget ?: false)
            .putBoolean(PREF_COLLAPSE_READ_MESSAGES, settings.collapseReadMessages ?: false)
            .putBoolean(PREF_COMPRESS, settings.compress ?: false)
            .putBoolean(PREF_CREDDIT_AUTORENEW, settings.credditAutoRenew ?: false)
            .putString(PREF_DEFAULT_COMMENT_SORT, settings.defaultCommentSort ?: "")
            .putBoolean(PREF_DOMAIN_DETAILS, settings.domainDetails ?: false)
            .putBoolean(PREF_EMAIL_MESSAGES, settings.emailMessages ?: false)
            .putBoolean(PREF_ENABLE_DEFAULT_THEMES, settings.enableDefaultThemes ?: false)
            .putBoolean(PREF_HIDE_ADS, settings.hideAds ?: false)
            .putBoolean(PREF_HIDE_DOWNS, settings.hideDowns ?: false)
            .putBoolean(PREF_HIDE_FROM_ROBOTS, settings.hideFromRobots ?: false)
            .putBoolean(PREF_HIDE_LOCATIONBAR, settings.hideLocationBar ?: false)
            .putBoolean(PREF_HIDE_UPS, settings.hideUps ?: false)
            .putBoolean(PREF_HIGHLIGHT_CONTROVERSIAL, settings.highlightControversial ?: false)
            .putBoolean(PREF_HIGHLIGHT_NEW_COMMENTS, settings.highlightNewComments ?: false)
            .putBoolean(PREF_IGNORE_SUGGESTED_SORT, settings.ignoreSuggestedSort ?: false)
            .putBoolean(PREF_LABEL_NSFW, settings.labelNsfw ?: false)
            .putString(PREF_LANG, settings.lang ?: "")
            .putBoolean(PREF_MARK_MESSAGES_READ, settings.markMessagesRead ?: false)
            .putString(PREF_MEDIA, settings.media)
            .putString(
                PREF_MIN_COMMENT_SCORE,
                if (settings.minCommentScore.toString() == "null") "" else settings.minCommentScore.toString()
            )
            .putString(
                PREF_MIN_LINK_SCORE,
                if (settings.minLinkScore.toString() == "null") "" else settings.minLinkScore.toString()
            )
            .putBoolean(PREF_MONITOR_MENTIONS, settings.monitorMentions ?: false)
            .putBoolean(PREF_NEWWINDOW, settings.newWindow ?: false)
            .putBoolean(PREF_NO_PROFANITY, settings.noProfanity ?: false)
            .putString(PREF_NUM_COMMENTS, settings.numComments.toString())
            .putString(PREF_NUMSITES, settings.numLinks.toString())
            .putBoolean(PREF_ORGANIC, settings.organic ?: false)
            .putBoolean(PREF_OVER_18, settings.over18 ?: false)
            .putBoolean(PREF_PUBLIC_FEEDS, settings.publicFeeds ?: false)
            .putBoolean(PREF_PUBLIC_VOTES, settings.publicVotes ?: false)
            .putBoolean(PREF_RESEARCH, settings.research ?: false)
            .putBoolean(PREF_SHOW_FLAIR, settings.showFlair ?: false)
            .putBoolean(PREF_SHOW_GOLD_EXPIRATION, settings.showGoldExpiration ?: false)
            .putBoolean(PREF_SHOW_LINK_FLAIR, settings.showLinkFlair ?: false)
            .putBoolean(PREF_SHOW_PROMOTE, settings.showPromote ?: false)
            .putBoolean(PREF_SHOW_STYLESHEETS, settings.showStylesheets ?: false)
            .putBoolean(PREF_SHOW_TRENDING, settings.showTrending ?: false)
            .putBoolean(PREF_STORE_VISITS, settings.storeVisits ?: false)
            .putString(PREF_THEME_SELECTOR, settings.themeSelector ?: "")
            .putBoolean(PREF_THREADED_MESSAGES, settings.threadedMessages ?: false)
            .putBoolean(PREF_USE_GLOBAL_DEFAULTS, settings.useGlobalDefaults ?: false)
            .apply()
    }

    override fun clearUserSettings() {
        // Only removing reddit preferences, app preferences can stay the same
        // Need to do this because PreferenceFragment can only show preferences from one SP instance
        sharedPreferences.edit()
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
            .apply()
    }

    override fun getDeviceId(): String {
        val sp = appContext.getSharedPreferences(PREFS_DEVICE_ID, Context.MODE_PRIVATE)
        var deviceId = sp.getString(PREF_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            sp.edit().putString(PREF_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    override fun askedForAnalytics(): Boolean {
        return appContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
            .getBoolean(PREF_ALLOW_ANALYTICS_ASKED, false)
    }

    override fun setAskedForAnalytics(b: Boolean) {
        appContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE).edit()
            .putBoolean(PREF_ALLOW_ANALYTICS_ASKED, b)
            .apply()
    }

    override fun getFont(): String? {
        return sharedPreferences.getString(PREF_FONT, "")
    }

    override fun getCommentSort(): String? {
        val default = appContext.getString(R.string.default_comment_sort)
        return sharedPreferences.getString(PREF_DEFAULT_COMMENT_SORT, default)
    }

    override fun saveCommentSort(pref: String) {
        sharedPreferences.edit()
            .putString(PREF_DEFAULT_COMMENT_SORT, pref)
            .apply()
    }

    override fun getMinCommentScore(): Int? {
        return try {
            sharedPreferences.getString(PREF_MIN_COMMENT_SCORE, null)
                ?.toInt()
        } catch (e: Exception) {
            // In all likelihood, this isn't an integer ("null")
            null
        }
    }

    override fun getShowControversiality(): Boolean {
        return sharedPreferences.getBoolean(PREF_HIGHLIGHT_CONTROVERSIAL, false)
    }

    override fun getOver18(): Boolean {
        return sharedPreferences.getBoolean(PREF_OVER_18, false)
    }

    override fun setOver18(b: Boolean) {
        sharedPreferences.edit()
            .putBoolean(PREF_OVER_18, b)
            .apply()
    }

    override fun getNoProfanity(): Boolean {
        return sharedPreferences.getBoolean(PREF_NO_PROFANITY, true)
    }

    override fun getLabelNsfw(): Boolean {
        return sharedPreferences.getBoolean(PREF_LABEL_NSFW, true)
    }

    override fun getColorScheme(): ColorScheme? {
        val defaultColorSchemeId = ColorScheme.STANDARD.id
        val id = sharedPreferences.getString(PREF_COLOR_SCHEME, defaultColorSchemeId) ?: defaultColorSchemeId
        return fromId(id)
    }

    override fun setColorScheme(colorScheme: ColorScheme) {
        val colorSchemeId = colorScheme.id
        sharedPreferences.edit()
            .putString(PREF_COLOR_SCHEME, colorSchemeId)
            .apply()
    }

    companion object {
        const val PREFS_USER = "prefs_user"

        // app settings
        const val PREFS_DEVICE_ID = "prefs_device_id"
        const val PREF_DEVICE_ID = "pref_device_id"
        const val PREF_ALLOW_ANALYTICS_ASKED = "pref_allow_analytics_asked"
        const val PREF_COLOR_SCHEME = "pref_color_scheme"
        const val PREF_FONT = "pref_font"

        // reddit settings
        const val PREF_HAS_FROM_REMOTE = "pref_flag_for_user"
        const val PREF_BETA = "beta"
        const val PREF_CLICKGAGDET = "clickgadget"
        const val PREF_COLLAPSE_READ_MESSAGES = "collapse_read_messages"
        const val PREF_COMPRESS = "compress"
        const val PREF_CREDDIT_AUTORENEW = "creddit_autorenew"
        const val PREF_DEFAULT_COMMENT_SORT = "default_comment_sort"
        const val PREF_DOMAIN_DETAILS = "domain_details"
        const val PREF_EMAIL_MESSAGES = "email_messages"
        const val PREF_ENABLE_DEFAULT_THEMES = "enable_default_themes"
        const val PREF_HIDE_ADS = "hide_ads"
        const val PREF_HIDE_DOWNS = "hide_downs"
        const val PREF_HIDE_FROM_ROBOTS = "hide_from_robots"
        const val PREF_HIDE_LOCATIONBAR = "hide_locationbar"
        const val PREF_HIDE_UPS = "hide_ups"
        const val PREF_HIGHLIGHT_CONTROVERSIAL = "highlight_controversial"
        const val PREF_HIGHLIGHT_NEW_COMMENTS = "highlight_new_comments"
        const val PREF_IGNORE_SUGGESTED_SORT = "ignore_suggested_sort"
        const val PREF_LABEL_NSFW = "label_nsfw"
        const val PREF_LANG = "lang"
        const val PREF_MARK_MESSAGES_READ = "mark_messages_read"
        const val PREF_MEDIA = "media"
        const val PREF_MIN_COMMENT_SCORE = "min_comment_score"
        const val PREF_MIN_LINK_SCORE = "min_link_score"
        const val PREF_MONITOR_MENTIONS = "monitor_mentions"
        const val PREF_NEWWINDOW = "newwindow"
        const val PREF_NO_PROFANITY = "no_profanity"
        const val PREF_NUM_COMMENTS = "num_comments"
        const val PREF_NUMSITES = "numsites"
        const val PREF_ORGANIC = "organic"
        const val PREF_OVER_18 = "over_18"
        const val PREF_PUBLIC_FEEDS = "public_feeds"
        const val PREF_PUBLIC_VOTES = "public_votes"
        const val PREF_RESEARCH = "research"
        const val PREF_SHOW_FLAIR = "show_flair"
        const val PREF_SHOW_GOLD_EXPIRATION = "show_gold_expiration"
        const val PREF_SHOW_LINK_FLAIR = "show_link_flair"
        const val PREF_SHOW_PROMOTE = "show_promote"
        const val PREF_SHOW_STYLESHEETS = "show_stylesheets"
        const val PREF_SHOW_TRENDING = "show_trending"
        const val PREF_STORE_VISITS = "store_visits"
        const val PREF_THEME_SELECTOR = "theme_selector"
        const val PREF_THREADED_MESSAGES = "threaded_messages"
        const val PREF_USE_GLOBAL_DEFAULTS = "use_global_defaults"
        val PREFS_REDDIT by lazy {
            listOf(
                "threaded_messages",
                "hide_downs",
                "email_messages",
                "show_link_flair",
                "creddit_autorenew",
                "show_trending",
                "private_feeds",
                "monitor_mentions",
                "research",
                "ignore_suggested_sort",
                "media",
                "clickgadget",
                "use_global_defaults",
                "label_nsfw",
                "domain_details",
                "show_stylesheets",
                "highlight_controversial",
                "no_profanity",
                "default_theme_sr",
                "lang",
                "hide_ups",
                "hide_from_robots",
                "compress",
                "store_visits",
                "beta",
                "show_gold_expiration",
                "over_18",
                "enable_default_themes",
                "show_promote",
                "min_comment_score",
                "public_votes",
                "organic",
                "collapse_read_messages",
                "show_flair",
                "mark_messages_read",
                "hide_ads",
                "min_link_score",
                "newwindow",
                "numsites",
                "num_comments",
                "highlight_new_comments",
                "default_comment_sort",
                "hide_locationbar",
            )
                .joinToString(separator = ",")
        }
    }
}
