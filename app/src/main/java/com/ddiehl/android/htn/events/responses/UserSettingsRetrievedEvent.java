package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.identity.UserSettings;

public class UserSettingsRetrievedEvent extends FailableEvent {

    private UserSettings mSettings;

    public UserSettings getSettings() {
        return mSettings;
    }
}
