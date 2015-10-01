package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class RefreshUserAccessTokenEvent {
    private String mRefreshToken;

    public RefreshUserAccessTokenEvent(@NonNull String refreshToken) {
        mRefreshToken = refreshToken;
    }

    @NonNull
    public String getRefreshToken() {
        return mRefreshToken;
    }
}
