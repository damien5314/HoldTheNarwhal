/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.content.Context;

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
import com.ddiehl.android.htn.utils.NUtils;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;

public class HTNAnalytics {

    private static final int FLURRY_SESSION_TIMEOUT_SECONDS = 30;

    public static void initializeFlurry(Context context) {
        FlurryAgent.init(context, NUtils.getFlurryApiKey(BuildConfig.DEBUG));
        FlurryAgent.setContinueSessionMillis(FLURRY_SESSION_TIMEOUT_SECONDS * 1000);
        FlurryAgent.setCaptureUncaughtExceptions(true);
        FlurryAgent.setLogEnabled(BuildConfig.DEBUG); // Disable Flurry logging for release builds

        FlurryAgent.setFlurryAgentListener(() -> {
            // Log initial Flurry event
            Map<String, String> params = new HashMap<>();

            UserIdentity identity = IdentityManager.getInstance(context).getUserIdentity();
            String userId = identity == null ?
                    "unauthorized" : BaseUtils.getMd5HexString(identity.getName());
            params.put("user", userId);
            FlurryAgent.setUserId(userId);

//            boolean adsEnabled = SettingsManager.getInstance(MainActivity.this).getAdsEnabled();
//            params.put("ads enabled", String.valueOf(adsEnabled));

            FlurryAgent.logEvent("session started", params);
        });
    }

    public static void startSession(Context context) {
        if (!RedditPrefs.areAnalyticsEnabled(context))
            return;
        FlurryAgent.onStartSession(context);
    }

    public static void endSession(Context context) {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.onEndSession(context);
    }

    public static void setUserIdentity(String name) {
//        if (!FlurryAgent.isSessionActive())
//            return;
        String encoded = name == null ? null : BaseUtils.getMd5HexString(name); // Always encode PII
        FlurryAgent.setUserId(encoded);
    }

    public static void logOpenLink(Link link) {
        if (!FlurryAgent.isSessionActive())
            return;
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", link.getSubreddit());
        params.put("id", link.getId());
        params.put("domain", link.getDomain());
        params.put("created", new Date(Double.valueOf(link.getCreatedUtc() * 1000).longValue()).toString());
        params.put("nsfw", String.valueOf(link.getOver18()));
        params.put("score", String.valueOf(link.getScore()));
        FlurryAgent.logEvent("open link", params);
    }

    public static void logOptionChangeSort() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("option - change sort");
    }

    public static void logOptionChangeSort(String sort) {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("option - change sort - " + sort);
    }

    public static void logOptionChangeTimespan() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("option - change timespan");
    }

    public static void logOptionChangeTimespan(String timespan) {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("option - change timespan - " + timespan);
    }

    public static void logOptionRefresh() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("option - refresh");
    }

    public static void logOptionSettings() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("option - settings");
    }

    public static void logDrawerNavigateToSubreddit() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - navigate to subreddit");
    }

    public static void logDrawerLogIn() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - log in");
    }

    public static void logDrawerUserProfile() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - user profile");
    }

    public static void logDrawerUserSubreddits() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - user subreddits");
    }

    public static void logDrawerFrontPage() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - navigate to front page");
    }

    public static void logDrawerAllSubreddits() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - navigate to /r/all");
    }

    public static void logDrawerRandomSubreddit() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("nav drawer - navigate to random subreddit");
    }

    public static void logClickedSignOut() {
        if (!FlurryAgent.isSessionActive())
            return;
        FlurryAgent.logEvent("clicked sign out");
    }

    public static void logSettingChanged(String key, String value) {
        if (!FlurryAgent.isSessionActive())
            return;
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("value", value);
        FlurryAgent.logEvent("setting changed", params);
    }

    public static void logApiError(RetrofitError error) {
        if (!FlurryAgent.isSessionActive())
            return;
        Map<String, String> params = new HashMap<>();
        params.put("url", error.getUrl());
        params.put("kind", error.getKind().toString());
        FlurryAgent.logEvent("api error", params);
    }

    ///////////////////////////////
    ////////// API Events /////////
    ///////////////////////////////

    @Subscribe
    public void onSignIn(UserIdentityRetrievedEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

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

    @Subscribe
    public void onSignOut(UserSignOutEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        FlurryAgent.logEvent("user signed out");
        FlurryAgent.setUserId(null);
    }

    @Subscribe
    public void onLoadSubreddit(LoadSubredditEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getSubreddit());
        params.put("sort", event.getSort());
        params.put("timespan", event.getTimeSpan());
        FlurryAgent.logEvent("view subreddit", params);
    }

    @Subscribe
    public void onLoadUserProfile(LoadUserProfileListingEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("show", event.getShow());
        params.put("user", BaseUtils.getMd5HexString(event.getUsername()));
        params.put("sort", event.getSort());
        params.put("timespan", event.getTimeSpan());
        FlurryAgent.logEvent("view user profile", params);
    }

    @Subscribe
    public void onLoadLinkComments(LoadLinkCommentsEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getSubreddit());
        params.put("article", event.getArticle());
        params.put("sort", event.getSort());
        params.put("comment", event.getCommentId());
        FlurryAgent.logEvent("load link comments", params);
    }

    @Subscribe
    public void onLoadMoreChildren(LoadMoreChildrenEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getLink().getSubreddit());
        params.put("article", event.getLink().getId());
        params.put("sort", event.getSort());
        FlurryAgent.logEvent("load more comment children", params);
    }

    @Subscribe
    public void onVote(VoteEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("type", event.getType());
        params.put("id", event.getListing().getId());
        params.put("direction", String.valueOf(event.getDirection()));
        FlurryAgent.logEvent("vote", params);
    }

    @Subscribe
    public void onSave(SaveEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("type", ((Listing) event.getListing()).getKind());
        params.put("id", ((Listing) event.getListing()).getId());
        params.put("category", event.getCategory());
        params.put("b", String.valueOf(event.isToSave()));
        FlurryAgent.logEvent("save", params);
    }

    @Subscribe
    public void onHide(HideEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        Map<String, String> params = new HashMap<>();
        params.put("type", ((Listing) event.getListing()).getKind());
        params.put("id", ((Listing) event.getListing()).getId());
        params.put("b", String.valueOf(event.isToHide()));
        FlurryAgent.logEvent("hide", params);
    }

    @Subscribe
    public void onReport(ReportEvent event) {
        if (!FlurryAgent.isSessionActive())
            return;

        // TODO Implement analytics event once feature is implemented
    }

    ////////////////////////////////
    /////////// Singleton //////////
    ////////////////////////////////

    private static HTNAnalytics _instance;

    private HTNAnalytics() { }

    public static HTNAnalytics getInstance() {
        if (_instance == null) {
            synchronized (HTNAnalytics.class) {
                if (_instance == null) {
                    _instance = new HTNAnalytics();
                }
            }
        }
        return _instance;
    }
}
