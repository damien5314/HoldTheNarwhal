/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.model;

import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Listing;

import java.util.Collection;
import java.util.List;

public interface CommentBank {

    boolean addAll(Collection<Listing> collection);
    boolean addAll(int index, Collection<Listing> collection);
    int indexOf(AbsComment obj);
    int visibleIndexOf(AbsComment obj);
    AbsComment get(int position);
    int size();
    AbsComment remove(int position);
    boolean remove(AbsComment comment);
    void clear();
    void setData(List<Listing> data);
    boolean isVisible(int position);
    int getNumVisible();
    AbsComment getVisibleComment(int position);
    void toggleThreadVisible(AbsComment comment);
    void collapseAllThreadsUnder(Integer score);
}
