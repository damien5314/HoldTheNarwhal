package com.ddiehl.reddit.identity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public abstract class AccessToken {
  private long mCreated = new Date().getTime();

  @Expose @SerializedName("access_token")
  protected String mToken;

  @Expose @SerializedName("token_type")
  protected String mTokenType;

  @Expose @SerializedName("expires_in")
  protected long mSecondsToExpiration;
  protected long mExpiration; // UTC

  @Expose @SerializedName("scope")
  protected String mScope;

  @Expose @SerializedName("refresh_token")
  protected String mRefreshToken;

  public String getToken() {
    return mToken;
  }

  public void setToken(String token) {
    mToken = token;
  }

  public String getTokenType() {
    return mTokenType;
  }

  public void setTokenType(String tokenType) {
    mTokenType = tokenType;
  }

  public long getExpiration() {
    if (mExpiration == 0) mExpiration = mSecondsToExpiration * 1000 + mCreated;
    return mExpiration;
  }

  public void setExpiration(long expiration) {
    mExpiration = expiration;
  }

  public String getScope() {
    return mScope;
  }

  public void setScope(String scope) {
    mScope = scope;
  }

  public String getRefreshToken() {
    return mRefreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    mRefreshToken = refreshToken;
  }

  public long secondsUntilExpiration() {
    return Math.max(0, (getExpiration() - System.currentTimeMillis()) / 1000);
  }

  public abstract boolean isUserAccessToken();
}