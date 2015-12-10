package com.ddiehl.android.htn.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;

import retrofit.Response;

public interface Analytics {
    void initialize();
    void setEnabled(boolean b);
    void startSession();
    void endSession();
    void setUserIdentity(@Nullable String name);
    void logOpenLink(@NonNull Link link);
    void logOptionChangeSort();
    void logOptionChangeSort(@NonNull String sort);
    void logOptionChangeTimespan();
    void logOptionChangeTimespan(@NonNull String timespan);
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
    void logSettingChanged(@NonNull String key, @NonNull String value);
    void logApiError(@Nullable Response error);
    void logSignIn(@NonNull UserIdentity identity);
    void logSignOut();
    void logLoadSubreddit(@Nullable String subreddit, @NonNull String sort, @NonNull String timespan);
    void logLoadUserProfile(@NonNull String show, @NonNull String sort, @NonNull String timespan);
    void logLoadLinkComments(@NonNull String sort);
    void logLoadMoreChildren(@NonNull String sort);
    void logVote(@NonNull String type, int direction);
    void logSave(@NonNull String type, @Nullable String category, boolean isSaving);
    void logHide(@NonNull String type, boolean isHiding);
    void logReport();
}
