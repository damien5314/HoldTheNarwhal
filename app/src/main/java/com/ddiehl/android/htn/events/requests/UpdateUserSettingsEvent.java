package com.ddiehl.android.htn.events.requests;

import java.util.Map;

public class UpdateUserSettingsEvent {

    private Map<String, String> mPrefs;

    public UpdateUserSettingsEvent(Map<String, String> prefs) {
        mPrefs = prefs;
    }

    public Map<String, String> getPrefs() {
        return mPrefs;
    }
}
