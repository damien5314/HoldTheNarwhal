package com.ddiehl.reddit.listings;

import com.ddiehl.reddit.identity.UserIdentity;
import com.google.gson.annotations.Expose;

public class UserIdentityListing extends Listing {
  @Expose UserIdentity data;

  public UserIdentity getUser() {
    return data;
  }

  @Override
  public String getId() {
    return data.getId();
  }

  @Override
  public String toString() {
    return getId() + " - " + getFullName() + " - Gold: " + data.isGold();
  }
}
