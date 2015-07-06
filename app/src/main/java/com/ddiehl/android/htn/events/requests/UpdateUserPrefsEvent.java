/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

import java.util.Map;

public class UpdateUserPrefsEvent {

    private Map<String, String> mPrefs;

    public UpdateUserPrefsEvent(Map<String, String> prefs) {
        mPrefs = prefs;
    }

    public Map<String, String> getPrefs() {
        return mPrefs;
    }
}
