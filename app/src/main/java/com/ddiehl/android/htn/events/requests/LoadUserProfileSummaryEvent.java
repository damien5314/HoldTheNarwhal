package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class LoadUserProfileSummaryEvent {

    private String mUsername;

    public LoadUserProfileSummaryEvent(@NonNull String username) {
        mUsername = username;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }
}
