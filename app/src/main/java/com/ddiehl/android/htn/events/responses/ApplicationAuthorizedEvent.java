package com.ddiehl.android.htn.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class ApplicationAuthorizedEvent extends FailableEvent {

    private AuthorizationResponse mResponse;

    public ApplicationAuthorizedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public ApplicationAuthorizedEvent(Exception error) {
        super(error);
    }

    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}