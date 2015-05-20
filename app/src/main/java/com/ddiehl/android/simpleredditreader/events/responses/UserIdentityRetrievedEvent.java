package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentityRetrievedEvent {
    private UserIdentity mUserIdentity;
    private Exception mError;
    private boolean mFailed = false;

    public UserIdentityRetrievedEvent(UserIdentity response) {
        mUserIdentity = response;
    }

    public UserIdentityRetrievedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
