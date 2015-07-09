/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
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
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.responses.UserAuthorizedEvent;
import com.ddiehl.android.htn.events.responses.UserSettingsRetrievedEvent;
import com.ddiehl.android.htn.view.BaseView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class SettingsFragment extends PreferenceFragment implements BaseView {

    private Bus mBus = BusProvider.getInstance();
    private AccessTokenManager mAccessTokenManager;
//    private IdentityManager mIdentityManager;
    private SettingsManager mSettingsManager;

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
//        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (mAccessTokenManager.hasUserAccessToken()) {
            refresh(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
//        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mSettingsManager.saveUserSettings(settings);
//        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        refresh(false);
        dismissSpinner();
    }

    @Subscribe
    public void onUserAuthorized(UserAuthorizedEvent event) {
        refresh(true);
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
