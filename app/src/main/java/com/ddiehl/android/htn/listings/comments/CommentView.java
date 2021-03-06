package com.ddiehl.android.htn.listings.comments;

import android.view.ContextMenu;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import rxreddit.model.Comment;
import rxreddit.model.Listing;

public interface CommentView {

    void showCommentContextMenu(ContextMenu menu, View v, Comment comment);

    void openShareView(@NotNull Comment comment);

    void openUserProfileView(@NotNull Comment comment);

    void openCommentInBrowser(@NotNull Comment comment);

    void openReplyView(@NotNull Listing listing);

    void openReportView(@NotNull Comment comment);
}
