package com.ddiehl.android.htn.view;

public interface LinkCommentsView extends LinkView, CommentView {
  void linkUpdated();
  void commentsUpdated();
  void commentUpdatedAt(int position);
  void commentRemovedAt(int position);
  void commentAddedAt(int position);
  void commentsUpdated(int position, int numItems);
  void commentsAddedAt(int position, int count);
  void commentsRemovedAt(int position, int count);
}
