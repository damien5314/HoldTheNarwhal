/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;

public interface LinkCommentsPresenter extends LinkPresenter, CommentPresenter {

    Link getLinkContext();
    void getComments();
    void getMoreChildren(CommentStub comment);
    void toggleThreadVisible(AbsComment comment);

    int getNumComments();
    AbsComment getComment(int position);

    String getSort();
    boolean getShowControversiality();
    void updateSort();
    void updateSort(String sort);

}
