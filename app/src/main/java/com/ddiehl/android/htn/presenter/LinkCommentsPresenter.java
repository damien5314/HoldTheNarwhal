package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Link;

public interface LinkCommentsPresenter
    extends BasePresenter, ListingsPresenter, LinkPresenter, CommentPresenter {
  Link getLinkContext();
  void getMoreComments(@NonNull CommentStub comment);
  void toggleThreadVisible(Comment comment);
  void onCommentSubmitted(@NonNull String commentText);
  boolean shouldShowParentLink();
}
