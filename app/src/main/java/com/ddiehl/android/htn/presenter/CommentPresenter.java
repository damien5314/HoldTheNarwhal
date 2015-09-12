/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;

public interface CommentPresenter {

    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Comment comment);
    void showCommentThread(@Nullable String subreddit, @Nullable String linkId, @NonNull String commentId);
    void getMoreChildren(@NonNull CommentStub comment);
    void openCommentPermalink();
    void openReplyView();
    void upvoteComment();
    void downvoteComment();
    void saveComment();
    void unsaveComment();
    void shareComment();
    void openCommentUserProfile();
    void openCommentUserProfile(@NonNull Comment comment);
    void openCommentInBrowser();
    void reportComment();
    void openCommentLink(@NonNull Comment comment);

}
