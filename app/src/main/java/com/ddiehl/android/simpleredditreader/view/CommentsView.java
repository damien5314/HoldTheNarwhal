package com.ddiehl.android.simpleredditreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;

public interface CommentsView extends BaseView {

    RecyclerView.Adapter<RecyclerView.ViewHolder> getListAdapter();
    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment);

    void openShareView(RedditComment comment);
    void openCommentInBrowser(RedditComment comment);
    void openReplyView(RedditComment comment);
}
