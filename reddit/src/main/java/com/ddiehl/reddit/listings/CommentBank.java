/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.listings;

import java.util.Collection;
import java.util.List;

public interface CommentBank {

    boolean addAll(Collection<Listing> collection);
    boolean addAll(int index, Collection<Listing> collection);
    int indexOf(AbsRedditComment obj);
    int visibleIndexOf(AbsRedditComment obj);
    AbsRedditComment get(int position);
    int size();
    AbsRedditComment remove(int position);
    boolean remove(AbsRedditComment comment);
    void clear();
    void setData(List<Listing> data);
    boolean isVisible(int position);
    int getNumVisible();
    AbsRedditComment getVisibleComment(int position);
    void toggleThreadVisible(AbsRedditComment comment);

}
