/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.RedditPrefs;
import com.ddiehl.android.htn.presenter.SettingsPresenter;
import com.ddiehl.android.htn.presenter.SettingsPresenterImpl;
import com.ddiehl.android.htn.view.MainView;
import com.squareup.otto.Bus;

public class SettingsFragment extends PreferenceFragment implements SettingsView {

    private Bus mBus = BusProvider.getInstance();
    private SettingsPresenter mSettingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(RedditPrefs.PREFS_USER);
        addPreferencesFromResource(R.xml.preferences);
        mSettingsPresenter = new SettingsPresenterImpl(getActivity(), this);
        mSettingsPresenter.updateAllPrefs();
    }

    @Override
    public Preference getRootPreference() {
        return getPreferenceScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(mSettingsPresenter);
    }

    @Override
    public void onStop() {
        mBus.unregister(mSettingsPresenter);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mSettingsPresenter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mSettingsPresenter);
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
