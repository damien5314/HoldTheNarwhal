package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizedEvent {
    private AuthorizationResponse mResponse;
    private Exception mError;
    private boolean mFailed = false;

    public UserAuthorizedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizedEvent(Exception error) {
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