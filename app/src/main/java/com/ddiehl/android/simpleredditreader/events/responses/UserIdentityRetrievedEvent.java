package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentityRetrievedEvent extends FailableEvent {
    private UserIdentity mUserIdentity;

    public UserIdentityRetrievedEvent(UserIdentity response) {
        mUserIdentity = response;
    }

    public UserIdentityRetrievedEvent(Exception error) {
        super(error);
    }

    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }
}
