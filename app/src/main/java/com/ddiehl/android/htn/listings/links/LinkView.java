package com.ddiehl.android.htn.listings.links;

import android.view.ContextMenu;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import rxreddit.model.Link;
import rxreddit.model.Media;

public interface LinkView {

    void showLinkContextMenu(ContextMenu menu, View view, Link link);

    void openUrlInWebView(@NotNull String url);

    void openRedditVideo(@NotNull Media.RedditVideo url);

    void showCommentsForLink(
            @NotNull String subreddit, @NotNull String linkId, @Nullable String commentId);

    void openShareView(@NotNull Link link);

    void openSubredditView(String subreddit);

    void openUserProfileView(@NotNull Link link);

    void openLinkInBrowser(@NotNull Link link);

    void openCommentsInBrowser(@NotNull Link link);

    void openReportView(@NotNull Link link);
}
