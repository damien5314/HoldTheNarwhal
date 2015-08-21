/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

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
import com.ddiehl.reddit.listings.Listing;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HTNAnalytics {

    private static HTNAnalytics _instance;

    private HTNAnalytics() { }

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
