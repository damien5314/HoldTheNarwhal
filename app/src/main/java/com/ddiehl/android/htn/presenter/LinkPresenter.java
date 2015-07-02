/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Link;

public interface LinkPresenter {

    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Link link);
    void openLink(Link link);
    void showCommentsForLink(Link link);
    void showCommentsForLink();
    void upvoteLink();
    void downvoteLink();
    void saveLink();
    void unsaveLink();
    void shareLink();
    void openLinkUserProfile();
    void openLinkUserProfile(Link link);
    void openLinkInBrowser();
    void openCommentsInBrowser();
    void hideLink();
    void unhideLink();
    void reportLink();

}
