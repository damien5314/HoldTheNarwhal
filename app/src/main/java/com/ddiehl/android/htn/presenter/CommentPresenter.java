/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;

public interface CommentPresenter {

    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Comment comment);
    void showCommentThread(String subreddit, String linkId, String commentId);
    void getMoreChildren(CommentStub comment);
    void openCommentPermalink();
    void openReplyView();
    void upvoteComment();
    void downvoteComment();
    void saveComment();
    void unsaveComment();
    void shareComment();
    void openCommentUserProfile();
    void openCommentUserProfile(Comment comment);
    void openCommentInBrowser();
    void reportComment();
    void openCommentLink(Comment comment);

}
