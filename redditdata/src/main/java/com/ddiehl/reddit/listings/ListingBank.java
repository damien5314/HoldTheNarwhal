package com.ddiehl.reddit.listings;

import java.util.Collection;
import java.util.List;

public interface ListingBank {

    boolean addAll(Collection<? extends Listing> collection);
    boolean addAll(int index, Collection<? extends Listing> collection);
    int indexOf(Object obj);
    int visibleIndexOf(Object obj);
    Listing get(int position);
    int size();
    Listing remove(int position);
    boolean remove(Listing comment);
    void clear();
    void setData(List<Listing> data);
    boolean isVisible(int position);
    int getNumVisible();
    Listing getVisibleComment(int position);
    void toggleThreadVisible(AbsRedditComment comment);

}
