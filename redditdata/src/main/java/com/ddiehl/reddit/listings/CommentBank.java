package com.ddiehl.reddit.listings;

import java.util.Collection;
import java.util.List;

public interface CommentBank {

    public boolean addAll(Collection<? extends AbsRedditComment> collection);
    public boolean addAll(int index, Collection<? extends AbsRedditComment> collection);
    public int indexOf(AbsRedditComment comment);
    public AbsRedditComment get(int position);
    public int size();
    public AbsRedditComment remove(int position);
    public boolean remove(AbsRedditComment comment);
    public void clear();
    public void setData(List<AbsRedditComment> data);
    public boolean isVisible(int position);
    public int getNumVisible();
    public AbsRedditComment getVisibleComment(int position);
    public void toggleThreadVisible(AbsRedditComment comment);

}
