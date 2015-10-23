package com.ddiehl.android.htn.analytics;

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

    @Subscribe @SuppressWarnings("unused")
    void logSignIn(UserIdentityRetrievedEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logSignOut(UserSignOutEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logLoadSubreddit(LoadSubredditEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logLoadUserProfile(LoadUserProfileListingEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logLoadLinkComments(LoadLinkCommentsEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logLoadMoreChildren(LoadMoreChildrenEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logVote(VoteEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logSave(SaveEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logHide(HideEvent event);

    @Subscribe @SuppressWarnings("unused")
    void logReport(ReportEvent event);
}
