package com.ddiehl.android.htn.events.responses;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.AuthorizationResponse;


public class ApplicationAuthorizedEvent extends FailableEvent {

    private AuthorizationResponse mResponse;

    public ApplicationAuthorizedEvent(@NonNull AuthorizationResponse response) {
        mResponse = response;
    }

    public ApplicationAuthorizedEvent(@NonNull Throwable error) {
        super(error);
    }

    @Nullable
    public AuthorizationResponse getResponse() {
        return mResponse;
    }
}