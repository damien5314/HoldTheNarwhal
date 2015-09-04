/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Comment;

public interface CommentView {

    void showCommentContextMenu(ContextMenu menu, View v, Comment comment);
    void showCommentThread(String subreddit, String linkId, String commentId);
    void openShareView(Comment comment);
    void openUserProfileView(Comment comment);
    void openCommentInBrowser(Comment comment);
    void openReplyView(Comment comment);
}
