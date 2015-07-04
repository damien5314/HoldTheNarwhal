package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.SettingsView;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

public class SettingsPresenterImpl implements SettingsPresenter {

    private Context mContext;
    private SettingsView mSettingsView;

    public SettingsPresenterImpl(Context c, SettingsView view) {
        mContext = c;
        mSettingsView = view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mSettingsView.updatePref(key);

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
