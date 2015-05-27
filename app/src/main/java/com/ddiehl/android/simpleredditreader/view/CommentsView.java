package com.ddiehl.android.simpleredditreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;

public interface CommentsView {

    void setTitle(String title);
    void showSpinner(String msg);
    void showSpinner(int resId);
    void dismissSpinner();
    void showToast(int resId);
    void showToast(String string);
    RecyclerView.Adapter<RecyclerView.ViewHolder> getListAdapter();
    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    void openShareView(RedditComment comment);
    void openCommentInBrowser(RedditComment comment);
}
