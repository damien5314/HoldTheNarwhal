package com.ddiehl.android.simpleredditreader;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadSubredditEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.simpleredditreader.events.requests.ReportEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.UserSignOutEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.ddiehl.reddit.identity.UserIdentity;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

public class HTNAnalytics {

    private static final String FLURRY_API_KEY = "***REMOVED***";
    private static final String FLURRY_API_KEY_DEBUG = "***REMOVED***";

    private static HTNAnalytics _instance;

    private HTNAnalytics() { }

    public void init(Context context) {
        FlurryAgent.setLogEnabled(BuildConfig.DEBUG); // Disable Flurry logging for release builds
        FlurryAgent.init(context, BuildConfig.DEBUG ? FLURRY_API_KEY_DEBUG : FLURRY_API_KEY);
        FlurryAgent.setContinueSessionMillis(30 * 1000); // Set Flurry session timeout to 30 seconds
    }

    @Subscribe
    public void onError(Exception e) {
        FlurryAgent.onError("error", e.toString(), e);
    }

    @Subscribe
    public void onUserIdentityRetrieved(UserIdentityRetrievedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        FlurryAgent.setUserId(BaseUtils.getMd5HexString(identity.getName()));
    }

    @Subscribe
    public void onSignOut(UserSignOutEvent event) {
        FlurryAgent.logEvent("user signed out");
        FlurryAgent.setUserId(null);
    }

    @Subscribe
    public void onLoadSubreddit(LoadSubredditEvent event) {
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", event.getSubreddit());
        params.put("sort", event.getSort());
        params.put("timespan", event.getTimeSpan());
        FlurryAgent.logEvent("view subreddit", params);
    }

    @Subscribe
    public void onLoadUserProfile(LoadUserProfileEvent event) {
        Map<String, String> params = new HashMap<>();
        params.put("show", event.getShow());
        params.put("user", BaseUtils.getMd5HexString(event.getUsername()));
        params.put("sort", event.getSort());
        params.put("timespan", event.getTimeSpan());
        FlurryAgent.logEvent("view user profile", params);
    }

    @Subscribe
    public void onLoadLinkComments(LoadLinkCommentsEvent event) {
        // TODO
    }

    @Subscribe
    public void onLoadMoreChildren(LoadMoreChildrenEvent event) {
        // TODO
    }

    @Subscribe
    public void onVote(VoteEvent event) {
        // TODO
    }

    @Subscribe
    public void onSave(SaveEvent event) {
        // TODO
    }

    @Subscribe
    public void onHide(HideEvent event) {
        // TODO
    }

    @Subscribe
    public void onReport(ReportEvent event) {
        // TODO
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
