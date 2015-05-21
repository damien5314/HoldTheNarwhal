package com.ddiehl.reddit.identity;


public class UserAccessToken extends AccessToken {
    @Override
    public boolean hasRefreshToken() {
        return mRefreshToken != null;
    }

    @Override
    public boolean isUserAccessToken() {
        return true;
    }
}
