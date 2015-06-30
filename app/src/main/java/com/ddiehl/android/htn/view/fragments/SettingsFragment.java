/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.design.widget.Snackbar;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.RedditPreferences;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(RedditPreferences.PREFS_USER);
        addPreferencesFromResource(R.xml.preferences);
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));

        // Show appreciation for users enabling ads
        if (key.equals("pref_enable_ads")) {
            if (sharedPreferences.getBoolean("pref_enable_ads", false)) {
                Snackbar.make(getView(), R.string.pref_enable_ads_thanks, Snackbar.LENGTH_SHORT).show();
            }
        }

        // Send Flurry event
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        Map prefs = sharedPreferences.getAll();
        params.put("value", String.valueOf(prefs.get(key)));
        FlurryAgent.logEvent("setting changed", params);
    }

}
