package com.ddiehl.android.simpleredditreader.events;

public class RefreshUserAccessTokenEvent {
    private String mRefreshToken;

    public RefreshUserAccessTokenEvent(String refreshToken) {
        mRefreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }
}
