package com.ddiehl.android.htn.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;

public interface CommentView extends BaseView {

    void showCommentContextMenu(ContextMenu menu, View v, RedditComment comment);
    void showCommentThread(String subreddit, String linkId, String commentId);
    void openShareView(RedditComment comment);
    void openUserProfileView(RedditComment comment);
    void openCommentInBrowser(RedditComment comment);
    void openReplyView(RedditComment comment);
}
