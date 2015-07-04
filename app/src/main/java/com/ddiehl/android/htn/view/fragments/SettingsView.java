package com.ddiehl.android.htn.view.fragments;

import android.preference.Preference;

import com.ddiehl.android.htn.view.BaseView;

public interface SettingsView extends BaseView {

    void updatePref(String key);
    void updatePref(Preference preference);

}
