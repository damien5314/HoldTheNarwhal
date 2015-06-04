package com.ddiehl.android.simpleredditreader.events.requests;

public class LoadUserOverviewEvent {

    String mUsername;

    public LoadUserOverviewEvent(String id) {
        mUsername = id;
    }

    public String getUsername() {
        return mUsername;
    }
}
