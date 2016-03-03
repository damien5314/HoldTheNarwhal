package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;

public interface LinkCommentsPresenter
    extends BasePresenter, ListingsPresenter, LinkPresenter, CommentPresenter {
  Link getLinkContext();
  void getMoreComments(@NonNull CommentStub comment);
  void toggleThreadVisible(Comment comment);
  void onCommentSubmitted(@NonNull String commentText);
}
