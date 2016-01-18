package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.List;

@Parcel @SuppressWarnings("unused")
public class ListingResponseData {
  @Expose
  List<Listing> children;
  @Expose
  String after;
  @Expose
  String before;
  String modhash;

  @ParcelConstructor
  public ListingResponseData() { }

  public ListingResponseData(List<Listing> messageList) {
    children = messageList;
  }

  public String getModhash() {
    return modhash;
  }

  public List<Listing> getChildren() {
    return children;
  }

  public String getAfter() {
    return after;
  }

  public String getBefore() {
    return before;
  }
}
