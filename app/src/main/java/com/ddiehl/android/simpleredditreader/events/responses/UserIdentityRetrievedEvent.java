package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.android.simpleredditreader.model.identity.UserIdentity;

import retrofit.RetrofitError;

public class UserIdentityRetrievedEvent {
    private UserIdentity mUserIdentity;
    private RetrofitError mError;
    private boolean mFailed = false;

    public UserIdentityRetrievedEvent(UserIdentity response) {
        mUserIdentity = response;
    }

    public UserIdentityRetrievedEvent(RetrofitError error) {
        mError = error;
        mFailed = true;
    }

    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }

    public RetrofitError getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
