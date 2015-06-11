package com.ddiehl.android.simpleredditreader.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;

public interface CommentView extends BaseView {

    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment);
    void navigateToCommentThread(String commentId);
    void openShareView(RedditComment comment);
    void openUserProfileView(RedditComment comment);
    void openCommentInBrowser(RedditComment comment);
    void openReplyView(RedditComment comment);
}
