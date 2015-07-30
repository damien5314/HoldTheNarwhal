/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;


import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizedEvent extends FailableEvent {
    private AuthorizationResponse mResponse;

    public UserAuthorizedEvent(AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizedEvent(Throwable e) {
        super(e);
    }

    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}