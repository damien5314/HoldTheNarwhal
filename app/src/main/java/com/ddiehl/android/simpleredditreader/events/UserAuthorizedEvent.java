package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.web.AuthTokenResponse;

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