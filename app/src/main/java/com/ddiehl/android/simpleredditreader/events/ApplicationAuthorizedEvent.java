package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.auth.AuthTokenResponse;

import retrofit.RetrofitError;


public class ApplicationAuthorizedEvent {
    private AuthTokenResponse mResponse;
    private RetrofitError mError;
    private boolean mFailed = false;

    public ApplicationAuthorizedEvent(AuthTokenResponse response) {
        mResponse = response;
    }

    public ApplicationAuthorizedEvent(RetrofitError error) {
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