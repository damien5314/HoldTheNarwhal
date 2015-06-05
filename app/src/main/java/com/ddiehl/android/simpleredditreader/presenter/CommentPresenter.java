package com.ddiehl.android.simpleredditreader.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditMoreComments;

public interface CommentPresenter {

    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment);
    void navigateToCommentThread(String commentId);
    void showMoreChildren(RedditMoreComments comment);
    void openReplyView();
    void upvote();
    void downvote();
    void saveComment();
    void unsaveComment();
    void shareComment();
    void openCommentInBrowser();
    void reportComment();

}
