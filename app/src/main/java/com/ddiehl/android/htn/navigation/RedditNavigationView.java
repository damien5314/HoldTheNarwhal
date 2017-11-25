package com.ddiehl.android.htn.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import rxreddit.model.PrivateMessage;

public interface RedditNavigationView {

    void showCommentsForLink(
            @NotNull String subreddit, @NotNull String linkId, @Nullable String commentId);

    void openURL(@NotNull String url);

    void showSettings();

    void showLoginView();

    void showInbox();

    void showInboxMessages(@NotNull List<PrivateMessage> messages);

    void showUserProfile(@NotNull String username, @Nullable String show, @Nullable String sort);

    void showSubredditNavigationView();

    void showUserSubreddits();

    void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan);

    void showSubredditImage(String url);
}
