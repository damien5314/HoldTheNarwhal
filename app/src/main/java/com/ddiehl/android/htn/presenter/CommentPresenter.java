package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;

public interface CommentPresenter extends BasePresenter {
  void showCommentContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Comment comment);
  void showCommentThread(
      @NonNull String subreddit, @NonNull String linkId, @NonNull String commentId);
  void getMoreComments(@NonNull CommentStub comment);
  void openCommentPermalink();
  void replyToComment();
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
