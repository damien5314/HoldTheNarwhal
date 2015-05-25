package com.ddiehl.android.simpleredditreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;

public interface CommentsView {

    void setTitle(String title);
    void showSpinner(String msg);
    void showSpinner(int resId);
    void dismissSpinner();
    void showToast(int resId);
    void showToast(String string);
    RecyclerView.Adapter<RecyclerView.ViewHolder> getListAdapter();
    void updateAdapter();
    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
}
