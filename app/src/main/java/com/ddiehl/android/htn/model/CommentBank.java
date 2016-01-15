package com.ddiehl.android.htn.model;

import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Listing;

import java.util.Collection;
import java.util.List;

public interface CommentBank {
  void add(AbsComment comment);
  void add(int index, AbsComment comment);
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
//  void toggleThreadVisible(int index, boolean visible);
  void toggleThreadVisible(Comment comment);
  void collapseAllThreadsUnder(@Nullable Integer score);
}
