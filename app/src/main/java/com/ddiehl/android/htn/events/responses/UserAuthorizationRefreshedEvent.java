package com.ddiehl.android.htn.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizationRefreshedEvent extends FailableEvent {
    private AuthorizationResponse mResponse;

    public UserAuthorizationRefreshedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizationRefreshedEvent(Throwable e) {
        super(e);
    }

    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}