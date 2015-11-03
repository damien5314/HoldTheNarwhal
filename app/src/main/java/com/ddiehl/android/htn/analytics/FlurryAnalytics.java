package com.ddiehl.android.htn.analytics;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.AndroidContextProvider;
import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.flurry.android.FlurryAgent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.Response;

public class FlurryAnalytics implements Analytics {
    private static final int FLURRY_SESSION_TIMEOUT_SECONDS = 30;

    private Logger mLogger = HoldTheNarwhal.getLogger();
    private Context mContext = AndroidContextProvider.getContext();
    private SettingsManager mSettingsManager;
    private boolean mInitialized = false;

    @Override
    public void initialize() {
        if (mInitialized) {
            mLogger.e("Analytics already initialized");
            return;
        }
        // Set this in init to avoid circular dependency
        mSettingsManager = HoldTheNarwhal.getSettingsManager();
        String apiKey = BuildConfig.FLURRY_API_KEY;
        FlurryAgent.init(mContext, apiKey);
        FlurryAgent.setContinueSessionMillis(FLURRY_SESSION_TIMEOUT_SECONDS * 1000);
        FlurryAgent.setCaptureUncaughtExceptions(true);
        FlurryAgent.setLogEnabled(BuildConfig.DEBUG); // Disable Flurry logging for release builds
        FlurryAgent.setFlurryAgentListener(this::onStartSession);
        mInitialized = true;
    }

    @Override
    public void startSession() {
        if (Build.VERSION.SDK_INT >= 14) return; // Sessions are handled automatically API 14+
        FlurryAgent.onStartSession(mContext);
    }

    private void onStartSession() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        // Log initial Flurry event
        Map<String, String> params = new HashMap<>();
        UserIdentity identity = HoldTheNarwhal.getIdentityManager().getUserIdentity();
        String userId = identity == null ?
                "unauthorized" : BaseUtils.getMd5HexString(identity.getName());
        params.put("user", userId);
        FlurryAgent.setUserId(userId);
        FlurryAgent.logEvent("session started", params);
    }

    @Override
    public void endSession() {
        if (Build.VERSION.SDK_INT >= 14) return; // Sessions are handled automatically API 14+
        FlurryAgent.onEndSession(mContext);
    }

    @Override
    public void setUserIdentity(String name) {
//        if (!mSettingsManager.areAnalyticsEnabled(mContext))
//            return;
        String encoded = name == null ? null : BaseUtils.getMd5HexString(name); // Always encode PII
        FlurryAgent.setUserId(encoded);
    }

    @Override
    public void logOpenLink(@NonNull Link link) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", link.getSubreddit());
        params.put("id", link.getId());
        params.put("domain", link.getDomain());
        long created = Double.valueOf(link.getCreatedUtc() * 1000).longValue();
        params.put("created", new Date(created).toString());
        params.put("nsfw", String.valueOf(link.getOver18()));
        params.put("score", String.valueOf(link.getScore()));
        FlurryAgent.logEvent("open link", params);
    }

    @Override
    public void logOptionChangeSort() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - change sort");
    }

    @Override
    public void logOptionChangeSort(@NonNull String sort) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - change sort - " + sort);
    }

    @Override
    public void logOptionChangeTimespan() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - change timespan");
    }

    @Override
    public void logOptionChangeTimespan(@NonNull String timespan) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - change timespan - " + timespan);
    }

    @Override
    public void logOptionRefresh() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - refresh");
    }

    @Override
    public void logOptionSettings() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - settings");
    }

    @Override
    public void logDrawerNavigateToSubreddit() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - navigate to subreddit");
    }

    @Override
    public void logDrawerLogIn() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - log in");
    }

    @Override
    public void logDrawerUserProfile() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - user profile");
    }

    @Override
    public void logDrawerUserSubreddits() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - user subreddits");
    }

    @Override
    public void logDrawerFrontPage() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - navigate to front page");
    }

    @Override
    public void logDrawerAllSubreddits() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - navigate to /r/all");
    }

    @Override
    public void logDrawerRandomSubreddit() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("nav drawer - navigate to random subreddit");
    }

    @Override
    public void logClickedSignOut() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("clicked sign out");
    }

    @Override
    public void logSettingChanged(@NonNull String key, @NonNull String value) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("value", value);
        FlurryAgent.logEvent("setting changed", params);
    }

    @Override
    public void logApiError(Response error) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("url", error.raw().request().url().toString());
        params.put("code", String.valueOf(error.code()));
        FlurryAgent.logEvent("api error", params);
    }

    ////////////////
    // API Events //
    ////////////////

    @Override
    public void logSignIn(@NonNull UserIdentity identity) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("user", BaseUtils.getMd5HexString(identity.getName()));
        Date date = new Date(Double.valueOf(identity.getCreatedUTC() * 1000).longValue());
        params.put("created", date.toString());
        params.put("gold", String.valueOf(identity.isGold()));
        params.put("link karma", String.valueOf(identity.getLinkKarma()));
        params.put("comment karma", String.valueOf(identity.getCommentKarma()));
        params.put("over 18", String.valueOf(identity.isOver18()));
        params.put("mod", String.valueOf(identity.isMod()));
        FlurryAgent.logEvent("user signed in", params);
    }

    @Override
    public void logSignOut() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("user signed out");
        FlurryAgent.setUserId(null);
    }

    @Override
    public void logLoadSubreddit(
            @Nullable String subreddit, @NonNull String sort, @NonNull String timespan) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", subreddit);
        params.put("sort", sort);
        params.put("timespan", timespan);
        FlurryAgent.logEvent("view subreddit", params);
    }

    @Override
    public void logLoadUserProfile(
            @NonNull String show, @NonNull String sort, @NonNull String timespan) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("show", show);
        params.put("sort", sort);
        params.put("timespan", timespan);
        FlurryAgent.logEvent("view user profile", params);
    }

    @Override
    public void logLoadLinkComments(@NonNull String sort) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("sort", sort);
        FlurryAgent.logEvent("load link comments", params);
    }

    @Override
    public void logLoadMoreChildren(@NonNull String sort) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("sort", sort);
        FlurryAgent.logEvent("load more comment children", params);
    }

    @Override
    public void logVote(@NonNull String type, int direction) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("direction", String.valueOf(direction));
        FlurryAgent.logEvent("vote", params);
    }

    @Override
    public void logSave(@NonNull String type, @Nullable String category, boolean isSaving) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("category", category);
        params.put("b", String.valueOf(isSaving));
        FlurryAgent.logEvent("save", params);
    }

    @Override
    public void logHide(@NonNull String type, boolean isHiding) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("b", String.valueOf(isHiding));
        FlurryAgent.logEvent("hide", params);
    }

    @Override
    public void logReport() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        // TODO: Implement analytics event once feature is implemented
    }

    ///////////////
    // Singleton //
    ///////////////

    private static FlurryAnalytics _instance;

    private FlurryAnalytics() { }

    public static FlurryAnalytics getInstance() {
        if (_instance == null) {
            synchronized (FlurryAnalytics.class) {
                if (_instance == null) {
                    _instance = new FlurryAnalytics();
                }
            }
        }
        return _instance;
    }
}
