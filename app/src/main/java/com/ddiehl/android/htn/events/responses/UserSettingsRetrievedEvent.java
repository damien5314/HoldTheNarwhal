package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserSettings;

public class UserSettingsRetrievedEvent extends FailableEvent {

    private UserSettings mSettings;

    public UserSettingsRetrievedEvent(@NonNull UserSettings settings) {
        mSettings = settings;
    }

    public UserSettingsRetrievedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public UserSettings getSettings() {
        return mSettings;
    }
}
