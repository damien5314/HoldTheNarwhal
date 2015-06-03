package com.ddiehl.android.simpleredditreader.view.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ddiehl.android.simpleredditreader.R;

/**
 * PreferenceFragment is preferred, but not supported until API 11.
 * Once we switch to TargetSDK 15 we can switch this to a PreferenceFragment implementation.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
