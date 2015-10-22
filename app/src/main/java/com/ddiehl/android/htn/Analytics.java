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
import com.ddiehl.reddit.listings.Link;
import com.squareup.otto.Subscribe;

import retrofit.Response;

public interface Analytics {
    void initialize(Context context);

    void startSession();

    void endSession();

    void setUserIdentity(String name);

    void logOpenLink(Link link);

    void logOptionChangeSort();

    void logOptionChangeSort(String sort);

    void logOptionChangeTimespan();

    void logOptionChangeTimespan(String timespan);

    void logOptionRefresh();

    void logOptionSettings();

    void logDrawerNavigateToSubreddit();

    void logDrawerLogIn();

    void logDrawerUserProfile();

    void logDrawerUserSubreddits();

    void logDrawerFrontPage();

    void logDrawerAllSubreddits();

    void logDrawerRandomSubreddit();

    void logClickedSignOut();

    void logSettingChanged(String key, String value);

    void logApiError(Response error);

    @Subscribe
    void onSignIn(UserIdentityRetrievedEvent event);

    @Subscribe
    void onSignOut(UserSignOutEvent event);

    @Subscribe
    void onLoadSubreddit(LoadSubredditEvent event);

    @Subscribe
    void onLoadUserProfile(LoadUserProfileListingEvent event);

    @Subscribe
    void onLoadLinkComments(LoadLinkCommentsEvent event);

    @Subscribe
    void onLoadMoreChildren(LoadMoreChildrenEvent event);

    @Subscribe
    void onVote(VoteEvent event);

    @Subscribe
    void onSave(SaveEvent event);

    @Subscribe
    void onHide(HideEvent event);

    @Subscribe
    void onReport(ReportEvent event);
}
