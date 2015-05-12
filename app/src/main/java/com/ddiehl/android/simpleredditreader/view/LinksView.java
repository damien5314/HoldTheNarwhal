package com.ddiehl.android.simpleredditreader.view;

import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

public interface LinksView {

    void setTitle(String title);
    void showSpinner(String msg);
    void dismissSpinner();
    void showToast(int resId);
    void showLink(Uri uri);
    void showCommentsForLink(RedditLink link);
    void updateAdapter();
    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    void openIntent(Intent i);
}
