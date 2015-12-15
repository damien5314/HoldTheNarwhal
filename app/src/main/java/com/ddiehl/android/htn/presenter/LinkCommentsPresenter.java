package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;

public interface LinkCommentsPresenter extends BasePresenter, LinkPresenter, CommentPresenter {
  Link getLinkContext();
  void requestData();
  void getMoreComments(@NonNull CommentStub comment);
  void toggleThreadVisible(@NonNull AbsComment comment);
  int getNumComments();
  AbsComment getComment(int position);
  String getSort();
  boolean getShowControversiality();
  void updateSort(@NonNull String sort);
  void onCommentSubmitted(@NonNull String parentId, @NonNull String commentText);
}
