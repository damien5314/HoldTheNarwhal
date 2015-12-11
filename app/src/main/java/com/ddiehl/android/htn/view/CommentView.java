package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Comment;

public interface CommentView {

  void showCommentContextMenu(ContextMenu menu, View v, Comment comment);
  void showCommentThread(@Nullable String subreddit, @Nullable String linkId, @NonNull String commentId);
  void openShareView(@NonNull Comment comment);
  void openUserProfileView(@NonNull Comment comment);
  void openCommentInBrowser(@NonNull Comment comment);
  void openReplyView(@NonNull Comment comment);
}
