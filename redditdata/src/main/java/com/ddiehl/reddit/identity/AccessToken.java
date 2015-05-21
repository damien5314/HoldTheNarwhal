package com.ddiehl.reddit.identity;

public abstract class AccessToken {

    protected String mToken;
    protected String mTokenType;
    protected long mExpiration; // UTC
    protected String mScope;
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
        return Math.max(0, (mExpiration - System.currentTimeMillis()) / 1000);
    }

    public abstract boolean hasRefreshToken();
    public abstract boolean isUserAccessToken();
}