package com.ddiehl.android.htn.navigation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import rxreddit.model.PrivateMessage;

public interface RedditNavigationView {

    void showCommentsForLink(
            @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId);

    void openURL(@NonNull String url);

    void showSettings();

    void showLoginView();

    void showInbox();

    void showInboxMessages(@NonNull List<PrivateMessage> messages);

    void showUserProfile(@NonNull String username, @Nullable String show, @Nullable String sort);

    void showSubredditNavigationView();

    void showUserSubreddits();

    void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan);

    void showSubredditImage(String url);
}
