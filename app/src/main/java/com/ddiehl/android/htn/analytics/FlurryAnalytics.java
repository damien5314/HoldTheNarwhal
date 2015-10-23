package com.ddiehl.android.htn.analytics;

import android.content.Context;
import android.os.Build;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileListingEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.flurry.android.FlurryAgent;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.Response;

public class FlurryAnalytics implements Analytics {
    private static final int FLURRY_SESSION_TIMEOUT_SECONDS = 30;

    private Context mContext;
    private SettingsManager mSettingsManager;
    private boolean mInitialized = false;

    @Override
    public void initialize(Context context) {
        if (mInitialized) {
            Logger.e("Analytics already initialized");
            return;
        }
        mContext = context.getApplicationContext();
        mSettingsManager = SettingsManager.getInstance(mContext);
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
        UserIdentity identity = IdentityManager.getInstance(mContext).getUserIdentity();
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
    public void logOpenLink(Link link) {
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
    public void logOptionChangeSort(String sort) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - change sort - " + sort);
    }

    @Override
    public void logOptionChangeTimespan() {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("option - change timespan");
    }

    @Override
    public void logOptionChangeTimespan(String timespan) {
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
    public void logSettingChanged(String key, String value) {
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
    @Subscribe
    public void logSignIn(UserIdentityRetrievedEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        UserIdentity identity = event.getUserIdentity();
        Map<String, String> params = new HashMap<>();
        params.put("user", BaseUtils.getMd5HexString(identity.getName()));
        params.put("created", new Date(Double.valueOf(identity.getCreatedUTC() * 1000).longValue()).toString());
        params.put("gold", String.valueOf(identity.isGold()));
        params.put("link karma", String.valueOf(identity.getLinkKarma()));
        params.put("comment karma", String.valueOf(identity.getCommentKarma()));
        params.put("over 18", String.valueOf(identity.isOver18()));
        params.put("mod", String.valueOf(identity.isMod()));
        FlurryAgent.logEvent("user signed in", params);
    }

    @Override
    @Subscribe
    public void logSignOut(UserSignOutEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        FlurryAgent.logEvent("user signed out");
        FlurryAgent.setUserId(null);
    }

    @Override
    @Subscribe
    public void logLoadSubreddit(LoadSubredditEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getSubreddit());
        params.put("sort", event.getSort());
        params.put("timespan", event.getTimeSpan());
        FlurryAgent.logEvent("view subreddit", params);
    }

    @Override
    @Subscribe
    public void logLoadUserProfile(LoadUserProfileListingEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("show", event.getShow());
        params.put("user", BaseUtils.getMd5HexString(event.getUsername()));
        params.put("sort", event.getSort());
        params.put("timespan", event.getTimeSpan());
        FlurryAgent.logEvent("view user profile", params);
    }

    @Override
    @Subscribe
    public void logLoadLinkComments(LoadLinkCommentsEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getSubreddit());
        params.put("article", event.getArticle());
        params.put("sort", event.getSort());
        params.put("comment", event.getCommentId());
        FlurryAgent.logEvent("load link comments", params);
    }

    @Override
    @Subscribe
    public void logLoadMoreChildren(LoadMoreChildrenEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getLink().getSubreddit());
        params.put("article", event.getLink().getId());
        params.put("sort", event.getSort());
        FlurryAgent.logEvent("load more comment children", params);
    }

    @Override
    @Subscribe
    public void logVote(VoteEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("type", event.getType());
        params.put("id", event.getListing().getId());
        params.put("direction", String.valueOf(event.getDirection()));
        FlurryAgent.logEvent("vote", params);
    }

    @Override
    @Subscribe
    public void logSave(SaveEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("type", ((Listing) event.getListing()).getKind());
        params.put("id", ((Listing) event.getListing()).getId());
        params.put("category", event.getCategory());
        params.put("b", String.valueOf(event.isToSave()));
        FlurryAgent.logEvent("save", params);
    }

    @Override
    @Subscribe
    public void logHide(HideEvent event) {
        if (!mSettingsManager.areAnalyticsEnabled()) return;
        Map<String, String> params = new HashMap<>();
        params.put("type", ((Listing) event.getListing()).getKind());
        params.put("id", ((Listing) event.getListing()).getId());
        params.put("b", String.valueOf(event.isToHide()));
        FlurryAgent.logEvent("hide", params);
    }

    @Override
    @Subscribe
    public void logReport(ReportEvent event) {
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
