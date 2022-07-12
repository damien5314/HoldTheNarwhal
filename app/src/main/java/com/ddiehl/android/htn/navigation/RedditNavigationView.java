package com.ddiehl.android.htn.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public interface RedditNavigationView {

    void openURL(@NotNull String url);

    void showUserSubreddits();

    void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan);

    void showSubredditImage(String url);
}
