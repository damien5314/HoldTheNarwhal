package com.ddiehl.android.htn.listings.comments;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import rxreddit.model.Comment;
import rxreddit.model.Listing;

public interface CommentView {

    void showCommentContextMenu(ContextMenu menu, View v, Comment comment);

    void openShareView(@NonNull Comment comment);

    void openUserProfileView(@NonNull Comment comment);

    void openCommentInBrowser(@NonNull Comment comment);

    void openReplyView(@NonNull Listing listing);
}
