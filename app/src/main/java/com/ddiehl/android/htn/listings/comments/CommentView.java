package com.ddiehl.android.htn.listings.comments;

import android.view.ContextMenu;
import android.view.View;

import rxreddit.model.Comment;

public interface CommentView {

    void showCommentContextMenu(ContextMenu menu, View v, Comment comment);
}
