/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

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
