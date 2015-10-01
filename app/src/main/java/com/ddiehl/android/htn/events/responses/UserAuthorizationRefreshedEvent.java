package com.ddiehl.android.htn.events.responses;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.AuthorizationResponse;


public class UserAuthorizationRefreshedEvent extends FailableEvent {
    private AuthorizationResponse mResponse;

    public UserAuthorizationRefreshedEvent(@NonNull AuthorizationResponse response) {
        mResponse = response;
    }

    public UserAuthorizationRefreshedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}