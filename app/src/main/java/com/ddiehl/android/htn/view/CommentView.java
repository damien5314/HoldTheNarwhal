package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Listing;

public interface CommentView {
  void showCommentContextMenu(ContextMenu menu, View v, Comment comment);
  void showCommentThread(
      @NonNull String subreddit, @NonNull String linkId, @NonNull String commentId);
  void openShareView(@NonNull Comment comment);
  void openUserProfileView(@NonNull Comment comment);
  void openCommentInBrowser(@NonNull Comment comment);
  void openReplyView(@NonNull Listing listing);
}
