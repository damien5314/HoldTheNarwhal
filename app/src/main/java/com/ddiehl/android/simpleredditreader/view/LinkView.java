package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditLink;

public interface LinkView {

    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link);
    void openWebViewForLink(RedditLink link);
    void showCommentsForLink(RedditLink link);
    void openShareView(RedditLink link);
    void openLinkInBrowser(RedditLink link);
    void openCommentsInBrowser(RedditLink link);

}
