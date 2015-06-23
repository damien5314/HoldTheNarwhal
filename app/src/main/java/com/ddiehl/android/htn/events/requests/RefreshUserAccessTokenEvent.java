package com.ddiehl.android.htn.events.requests;

public class RefreshUserAccessTokenEvent {
    private String mRefreshToken;

    public RefreshUserAccessTokenEvent(String refreshToken) {
        mRefreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }
}
