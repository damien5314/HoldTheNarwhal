package com.ddiehl.android.htn.model;

import com.ddiehl.reddit.listings.Listing;

import java.util.ArrayList;
import java.util.Collection;

public class ListingList extends ArrayList<Listing> {

  @Override
  public boolean add(Listing object) {
    boolean b = super.add(object);
    syncPositions();
    return b;
  }

  @Override
  public void add(int index, Listing object) {
    super.add(index, object);
    syncPositions();
  }

  @Override
  public boolean addAll(Collection<? extends Listing> collection) {
    boolean b = super.addAll(collection);
    syncPositions();
    return b;
  }

  @Override
  public boolean addAll(int index, Collection<? extends Listing> collection) {
    boolean b = super.addAll(index, collection);
    syncPositions();
    return b;
  }

  @Override
  public Listing remove(int index) {
    Listing listing = super.remove(index);
    syncPositions();
    return listing;
  }

  @Override
  public boolean remove(Object object) {
    boolean b = super.remove(object);
    syncPositions();
    return b;
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    super.removeRange(fromIndex, toIndex);
    syncPositions();
  }

  private void syncPositions() {
    for (int i = 0; i < size(); i++) get(i).setPosition(i);
  }
}
