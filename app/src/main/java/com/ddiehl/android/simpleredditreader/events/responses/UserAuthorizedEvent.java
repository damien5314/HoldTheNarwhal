package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.AuthTokenResponse;

import retrofit.RetrofitError;


public class UserAuthorizedEvent {
    private AuthTokenResponse mResponse;
    private RetrofitError mError;
    private boolean mFailed = false;

    public UserAuthorizedEvent(AuthTokenResponse response) {
        mResponse = response;
    }

    public UserAuthorizedEvent(RetrofitError error) {
        mError = error;
        mFailed = true;
    }

    public AuthTokenResponse getResponse() {
        return mResponse;
    }

    public RetrofitError getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}