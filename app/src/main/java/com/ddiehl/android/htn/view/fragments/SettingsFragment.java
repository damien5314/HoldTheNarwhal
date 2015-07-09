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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.GetUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.UpdateUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.responses.UserSettingsRetrievedEvent;
import com.ddiehl.android.htn.view.BaseView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment
        implements BaseView, SharedPreferences.OnSharedPreferenceChangeListener {

    private Bus mBus = BusProvider.getInstance();
    private AccessTokenManager mAccessTokenManager;
//    private IdentityManager mIdentityManager;
    private SettingsManager mSettingsManager;

    private boolean mIsChanging = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mAccessTokenManager = AccessTokenManager.getInstance(getActivity());
//        mIdentityManager = IdentityManager.getInstance(getActivity());
        mSettingsManager = SettingsManager.getInstance(getActivity());

        getPreferenceManager().setSharedPreferencesName(SettingsManager.PREFS_USER);
        addPreferencesFromResource(R.xml.preferences_all); // Required for getSharedPreferences()
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        mBus.unregister(mSettingsManager);
        getActivity().setTitle(R.string.settings_fragment_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (mAccessTokenManager.hasUserAccessToken()) {
            refresh(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        mBus.register(mSettingsManager);
        mBus.unregister(this);
        super.onStop();
    }

    private void refresh(boolean pullFromServer) {
        getActivity().invalidateOptionsMenu();
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.preferences_all);
        if (mSettingsManager.hasFromRemote()) {
            addUserPreferences();
        }
        updateAllPrefSummaries();
        if (pullFromServer) {
            getData();
        }
    }

    private void addUserPreferences() {
        addPreferencesFromResource(R.xml.preferences_user);
        UserIdentity user = IdentityManager.getInstance(getActivity()).getUserIdentity();
        if (user != null && user.isGold()) {
            addPreferencesFromResource(R.xml.preferences_user_gold);
        }
    }

    private void getData() {
        showSpinner(null);
        mBus.post(new GetUserSettingsEvent());
    }

    @Subscribe
    public void onSettingsRetrieved(UserSettingsRetrievedEvent event) {
        if (event.isFailed()) {
            dismissSpinner();
            return;
        }

        UserSettings settings = event.getSettings();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mSettingsManager.saveUserSettings(settings);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        refresh(false);
        dismissSpinner();
    }

    @Subscribe
    public void onUserSignOut(UserSignOutEvent event) {
        refresh(false);
    }

    private void updateAllPrefSummaries() {
        Preference root = getPreferenceScreen();
        updatePrefSummary(root);
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                updatePrefSummary(pGrp.getPreference(i));
            }
        } else {
            if (p instanceof ListPreference) {
                ListPreference listPref = (ListPreference) p;
                p.setSummary(listPref.getEntry());
            } else if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                String s = editTextPref.getText();
                p.setSummary(s.equals("null") ? "" : s);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        updatePrefSummary(findPreference(key));

        if (mIsChanging || !mAccessTokenManager.hasUserAccessToken())
            return;
        mIsChanging = true;

        Map<String, String> changedSettings = new HashMap<>(); // Track changed keys and values

        switch (key) {
            case SettingsManager.PREF_ENABLE_ADS:
                if (sp.getBoolean(SettingsManager.PREF_ENABLE_ADS, false)) {
                    // Show appreciation for users enabling ads
                    showToast(R.string.pref_enable_ads_thanks);
                }
                break;
            default:
                Preference p = findPreference(key);
                if (p instanceof CheckBoxPreference) {
                    boolean value = ((CheckBoxPreference) p).isChecked();
                    changedSettings.put(key, String.valueOf(value));
                } else if (p instanceof ListPreference) {
                    String value = ((ListPreference) p).getValue();
                    changedSettings.put(key, value);
                } else if (p instanceof EditTextPreference) {
                    String value = ((EditTextPreference) p).getEditText().getText().toString();
                    changedSettings.put(key, value);
                }
                break;
        }

        // Force "make safe(r) for work" to be true if "over 18" is false
        if (!sp.getBoolean(SettingsManager.PREF_OVER_18, false)) {
            CheckBoxPreference pref = ((CheckBoxPreference) findPreference(SettingsManager.PREF_NO_PROFANITY));
            if (!pref.isChecked()) {
                changedSettings.put(SettingsManager.PREF_NO_PROFANITY, String.valueOf(true));
                pref.setChecked(true);
            }
        }

        // Force "label nsfw" to be true if "make safe(r) for work" is true
        if (sp.getBoolean(SettingsManager.PREF_NO_PROFANITY, true)) {
            CheckBoxPreference pref = ((CheckBoxPreference) findPreference(SettingsManager.PREF_LABEL_NSFW));
            if (!pref.isChecked()) {
                changedSettings.put(SettingsManager.PREF_LABEL_NSFW, String.valueOf(true));
                pref.setChecked(true);
            }
        }

        if (changedSettings.size() > 0) {
            // Post SettingsUpdate event with changed keys and values
            mBus.post(new UpdateUserSettingsEvent(changedSettings));
        }

        // Send Flurry event
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        Map prefs = sp.getAll();
        params.put("value", String.valueOf(prefs.get(key)));
        FlurryAgent.logEvent("setting changed", params);

        mIsChanging = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_refresh).setVisible(mSettingsManager.hasFromRemote());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
