package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizedEvent extends FailableEvent {
    private AuthorizationResponse mResponse;

    public UserAuthorizedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizedEvent(Exception e) {
        super(e);
    }

    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}