package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

import java.util.Map;

public class UpdateUserSettingsEvent {

    private Map<String, String> mPrefs;

    public UpdateUserSettingsEvent(@NonNull Map<String, String> prefs) {
        mPrefs = prefs;
    }

    @NonNull
    public Map<String, String> getPrefs() {
        return mPrefs;
    }
}
