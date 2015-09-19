/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Link;

public interface LinkPresenter extends BasePresenter {

    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Link link);
    void openLink(@NonNull Link link);
    void showCommentsForLink(@NonNull Link link);
    void showCommentsForLink();
    void upvoteLink();
    void downvoteLink();
    void saveLink();
    void unsaveLink();
    void shareLink();
    void openLinkUserProfile();
    void openLinkUserProfile(@NonNull Link link);
    void openLinkInBrowser();
    void openCommentsInBrowser();
    void hideLink();
    void unhideLink();
    void reportLink();

}
