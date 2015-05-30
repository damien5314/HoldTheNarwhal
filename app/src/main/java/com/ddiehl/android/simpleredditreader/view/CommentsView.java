package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;

public interface CommentsView extends BaseView {

    void commentsUpdated();
    void commentUpdatedAt(int position);
    void commentRemovedAt(int position);

    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment);
    void openShareView(RedditComment comment);
    void openCommentInBrowser(RedditComment comment);
    void openReplyView(RedditComment comment);

}
