package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.List;

@Parcel
public class ListingResponse {
  @Expose
  String kind;
  @Expose
  ListingResponseData data;

  @ParcelConstructor
  public ListingResponse() { }

  public ListingResponse(List<Listing> messageList) {
    data = new ListingResponseData(messageList);
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public ListingResponseData getData() {
    return data;
  }

  public void setData(ListingResponseData data) {
    this.data = data;
  }
}
