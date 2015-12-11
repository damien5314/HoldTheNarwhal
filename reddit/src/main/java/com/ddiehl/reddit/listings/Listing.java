package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

public abstract class Listing<T extends Listing.Data> {

  @Expose protected String kind;
  @Expose protected T data;

  public String getId() {
    return data.id;
  }

  public String getKind() {
    return kind;
  }

  public String getName() {
    return kind + "_" + data.id;
  }

  public static abstract class Data {
    @Expose protected String id;
  }
}
