package com.ddiehl.android.htn.events.responses;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizedEvent extends FailableEvent {
    private AuthorizationResponse mResponse;

    public UserAuthorizedEvent(@NonNull AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}