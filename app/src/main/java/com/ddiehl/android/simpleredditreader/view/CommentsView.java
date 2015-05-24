package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

public interface CommentsView {

    void setTitle(String title);
    void showSpinner(String msg);
    void dismissSpinner();
    void showToast(int resId);
    void showToast(String string);
    void updateAdapter();
    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
}
