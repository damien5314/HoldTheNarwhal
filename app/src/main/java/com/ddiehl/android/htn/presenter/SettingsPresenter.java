package com.ddiehl.android.htn.presenter;

import android.content.SharedPreferences;
import android.preference.Preference;

public interface SettingsPresenter extends SharedPreferences.OnSharedPreferenceChangeListener {

    void updateAllPrefs();
    void updatePref(Preference p);

}
