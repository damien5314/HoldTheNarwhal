package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.identity.UserSettings;

public class UserSettingsRetrievedEvent extends FailableEvent {

    private UserSettings mSettings;

    public UserSettingsRetrievedEvent(UserSettings settings) {
        mSettings = settings;
    }

    public UserSettingsRetrievedEvent(Throwable e) {
        super(e);
    }

    public UserSettings getSettings() {
        return mSettings;
    }
}
