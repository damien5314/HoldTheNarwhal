package com.ddiehl.android.htn.model;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import rxreddit.model.AbsComment;
import rxreddit.model.Comment;
import rxreddit.model.Listing;

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
  void toggleThreadVisible(Comment comment);
  void collapseAllThreadsUnder(@Nullable Integer score);
}
