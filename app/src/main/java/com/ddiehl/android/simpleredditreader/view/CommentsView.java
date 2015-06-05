package com.ddiehl.android.simpleredditreader.view;

public interface CommentsView extends CommentView {

    void commentsUpdated();
    void commentUpdatedAt(int position);
    void commentRemovedAt(int position);

}
