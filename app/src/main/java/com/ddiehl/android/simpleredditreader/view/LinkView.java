package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditLink;

public interface LinkView extends BaseView {

    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link);
    void openLinkInWebView(RedditLink link);
    void showCommentsForLink(String subreddit, String linkId, String commentId);
    void openShareView(RedditLink link);
    void openUserProfileView(RedditLink link);
    void openLinkInBrowser(RedditLink link);
    void openCommentsInBrowser(RedditLink link);

}
