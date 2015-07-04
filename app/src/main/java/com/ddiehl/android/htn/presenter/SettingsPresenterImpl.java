package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;

import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.SettingsView;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

public class SettingsPresenterImpl implements SettingsPresenter {

    private Context mContext;
    private SettingsView mSettingsView;
    private IdentityManager mIdentityManager;

    public SettingsPresenterImpl(Context c, SettingsView view) {
        mContext = c;
        mSettingsView = view;
        mIdentityManager = IdentityManager.getInstance(mContext);
    }

    @Override
    public void updateAllPrefs() {
        Preference root = mSettingsView.getRootPreference();
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

    @Override
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePref(((Preference) sharedPreferences.getAll().get(key)));

        // Show appreciation for users enabling ads
        if (key.equals("pref_enable_ads")) {
            if (sharedPreferences.getBoolean("pref_enable_ads", false)) {
                mSettingsView.showToast(R.string.pref_enable_ads_thanks);
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
