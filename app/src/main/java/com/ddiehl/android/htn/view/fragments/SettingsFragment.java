/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.RedditPrefs;
import com.ddiehl.android.htn.view.BaseView;
import com.ddiehl.android.htn.view.MainView;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment
        implements BaseView, SharedPreferences.OnSharedPreferenceChangeListener {

    private Bus mBus = BusProvider.getInstance();
    private IdentityManager mIdentityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(RedditPrefs.PREFS_USER);
        addPreferencesFromResource(R.xml.preferences);
        mIdentityManager = IdentityManager.getInstance(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        updateAllPrefs();
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
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    public void updateAllPrefs() {
        Preference root = getPreferenceScreen();
        updateAllPrefs(root);
    }

    private void updateAllPrefs(Preference root) {
        if (root instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) root;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                updateAllPrefs(pGrp.getPreference(i));
            }
        } else {
            updatePref(root);
        }

    }

    public void updatePref(Preference p) {
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
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        updatePref(findPreference(key));

        // Show appreciation for users enabling ads
        if (key.equals(RedditPrefs.PREF_ENABLE_ADS)) {
            if (sp.getBoolean(RedditPrefs.PREF_ENABLE_ADS, false)) {
                showToast(R.string.pref_enable_ads_thanks);
            }
        }

        if (!sp.getBoolean(RedditPrefs.PREF_OVER_18, false)) {
            ((CheckBoxPreference) findPreference(RedditPrefs.PREF_NO_PROFANITY)).setChecked(true);
        }

        if (sp.getBoolean(RedditPrefs.PREF_NO_PROFANITY, true)) {
            ((CheckBoxPreference) findPreference(RedditPrefs.PREF_LABEL_NSFW)).setChecked(true);
        }

        // Send Flurry event
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        Map prefs = sp.getAll();
        params.put("value", String.valueOf(prefs.get(key)));
        FlurryAgent.logEvent("setting changed", params);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    @Override
    public void showSpinner(String msg) {
        ((MainView) getActivity()).showSpinner(msg);
    }

    @Override
    public void showSpinner(int resId) {
        ((MainView) getActivity()).showSpinner(resId);
    }

    @Override
    public void dismissSpinner() {
        ((MainView) getActivity()).dismissSpinner();
    }

    @Override
    public void showToast(String msg) {
        ((MainView) getActivity()).showToast(msg);
    }

    @Override
    public void showToast(int resId) {
        ((MainView) getActivity()).showToast(resId);
    }
}
