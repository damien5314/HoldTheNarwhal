/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view;

public interface LinkCommentsView extends LinkView, CommentView {

    void linkUpdated();
    void commentsUpdated();
    void commentUpdatedAt(int position);
    void commentRemovedAt(int position);

}
