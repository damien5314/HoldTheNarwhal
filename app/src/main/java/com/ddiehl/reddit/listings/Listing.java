package com.ddiehl.reddit.listings;

import com.ddiehl.reddit.adapters.ListingParcelConverter;
import com.google.gson.annotations.Expose;

import org.parceler.Parcel;

@Parcel(converter = ListingParcelConverter.class)
public abstract class Listing {
  @Expose
  protected String kind;

  public abstract String getId();

  public String getFullName() {
    return getKind() + "_" + getId();
  }

  public String getKind() {
    return kind;
  }
}
