package com.ddiehl.android.htn.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import rxreddit.model.PrivateMessage;

@Deprecated
public interface RedditNavigationView {

    void openURL(@NotNull String url);

    void showInboxMessages(@NotNull List<PrivateMessage> messages);

    void showUserProfile(@NotNull String username, @Nullable String show, @Nullable String sort);

    void showSubredditNavigationView();

    void showUserSubreddits();

    void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan);

    void showSubredditImage(String url);
}
