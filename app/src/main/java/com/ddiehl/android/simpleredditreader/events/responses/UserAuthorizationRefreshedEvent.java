package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizationRefreshedEvent extends FailableEvent {
    private AuthorizationResponse mResponse;

    public UserAuthorizationRefreshedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizationRefreshedEvent(Exception e) {
        super(e);
    }

    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}