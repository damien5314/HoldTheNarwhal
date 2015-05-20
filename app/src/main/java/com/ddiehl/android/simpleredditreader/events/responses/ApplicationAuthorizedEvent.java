package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.AuthTokenResponse;


public class ApplicationAuthorizedEvent {
    private AuthTokenResponse mResponse;
    private Exception mError;
    private boolean mFailed = false;

    public ApplicationAuthorizedEvent(AuthTokenResponse response) {
        mResponse = response;
    }

    public ApplicationAuthorizedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public AuthTokenResponse getResponse() {
        return mResponse;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}