package com.ddiehl.reddit.listings;

import java.util.Collection;
import java.util.List;

public interface CommentBank {

    boolean addAll(Collection<? extends AbsRedditComment> collection);
    boolean addAll(int index, Collection<? extends AbsRedditComment> collection);
    int indexOf(Object obj);
    AbsRedditComment get(int position);
    int size();
    AbsRedditComment remove(int position);
    boolean remove(AbsRedditComment comment);
    void clear();
    void setData(List<AbsRedditComment> data);
    boolean isVisible(int position);
    int getNumVisible();
    AbsRedditComment getVisibleComment(int position);
    void toggleThreadVisible(AbsRedditComment comment);

}
