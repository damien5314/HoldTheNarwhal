package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class ApplicationAuthorizedEvent {
    private AuthorizationResponse mResponse;
    private Exception mError;
    private boolean mFailed = false;

    public ApplicationAuthorizedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public ApplicationAuthorizedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public AuthorizationResponse getResponse() {
        return mResponse;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}