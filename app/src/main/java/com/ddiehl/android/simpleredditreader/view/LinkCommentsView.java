package com.ddiehl.android.simpleredditreader.view;

public interface LinkCommentsView extends LinkView, CommentView {

    void linkUpdated();
    void commentsUpdated();
    void commentUpdatedAt(int position);
    void commentRemovedAt(int position);

}
