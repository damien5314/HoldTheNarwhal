package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

public interface LinksView {

    void setTitle(String title);
    void showSpinner(String msg);
    void dismissSpinner();
    void showToast(int resId);
    void openWebViewForLink(RedditLink link);
    void showCommentsForLink(RedditLink link);
    void updateAdapter();
    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    void openShareView(RedditLink link);
    void openLinkInBrowser(RedditLink link);
    void openCommentsInBrowser(RedditLink link);
}
