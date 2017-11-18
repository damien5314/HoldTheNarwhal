package com.ddiehl.android.htn.listings.links;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.View;

import rxreddit.model.Link;

public interface LinkView {

    void showLinkContextMenu(ContextMenu menu, View view, Link link);

    void openUrlInWebView(@NonNull String url);

    void showCommentsForLink(
            @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId);

    void openShareView(@NonNull Link link);

    void openSubredditView(String subreddit);

    void openUserProfileView(@NonNull Link link);

    void openLinkInBrowser(@NonNull Link link);

    void openCommentsInBrowser(@NonNull Link link);

    void openReportView(@NonNull Link link);
}
