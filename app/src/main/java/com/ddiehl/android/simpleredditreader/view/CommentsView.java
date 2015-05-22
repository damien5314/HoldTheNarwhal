package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

public interface CommentsView {

    void setTitle(String title);
    void updateAdapter();
    void showSpinner(String msg);
    void dismissSpinner();
    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
}
