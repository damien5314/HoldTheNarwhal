package com.ddiehl.android.simpleredditreader.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditLink;

public interface LinkPresenter {

    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link);
    void openLink(RedditLink link);
    void showCommentsForLink(RedditLink link);
    void showCommentsForLink();
    void upvoteLink();
    void downvoteLink();
    void saveLink();
    void unsaveLink();
    void shareLink();
    void openLinkUserProfile(RedditLink link);
    void openLinkInBrowser();
    void openCommentsInBrowser();
    void hideLink();
    void unhideLink();
    void reportLink();

}
