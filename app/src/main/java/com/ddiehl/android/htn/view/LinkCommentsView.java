package com.ddiehl.android.htn.view;

public interface LinkCommentsView extends ListingsView, LinkView, CommentView {

  String getCommentId();

  String getArticleId();

  String getSubreddit();

  String getSort();
}
