/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Link;

public interface LinkView extends BaseView {

    void showLinkContextMenu(ContextMenu menu, View v, Link link);
    void openLinkInWebView(Link link);
    void showCommentsForLink(String subreddit, String linkId, String commentId);
    void openShareView(Link link);
    void openUserProfileView(Link link);
    void openLinkInBrowser(Link link);
    void openCommentsInBrowser(Link link);

}
