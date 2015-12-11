package com.ddiehl.reddit.identity;


public class ApplicationAccessToken extends AccessToken {
  @Override
  public boolean isUserAccessToken() {
    return false;
  }
}
