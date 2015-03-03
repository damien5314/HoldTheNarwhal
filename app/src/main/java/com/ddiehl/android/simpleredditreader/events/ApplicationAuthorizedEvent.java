package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.web.AccessTokenResponse;

import retrofit.RetrofitError;


public class ApplicationAuthorizedEvent {
    private AccessTokenResponse mResponse;
    private RetrofitError mError;
    private boolean mFailed = false;

    public ApplicationAuthorizedEvent(AccessTokenResponse response) {
        mResponse = response;
    }

    public ApplicationAuthorizedEvent(RetrofitError error) {
        mError = error;
        mFailed = true;
    }

    public AccessTokenResponse getResponse() {
        return mResponse;
    }

    public RetrofitError getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}