package com.ddiehl.android.htn.view.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManagerImpl;
import com.ddiehl.android.htn.presenter.SettingsPresenter;
import com.ddiehl.android.htn.presenter.SettingsPresenterImpl;
import com.ddiehl.android.htn.view.MenuTintUtils;
import com.ddiehl.android.htn.view.SettingsView;
import com.ddiehl.android.htn.view.activities.AboutAppActivity;

import javax.inject.Inject;

import rxreddit.model.UserIdentity;

public class SettingsFragment extends PreferenceFragment
        implements SettingsView, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Inject IdentityManager mIdentityManager;
    SettingsPresenter mSettingsPresenter;

    ProgressDialog mLoadingOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        mSettingsPresenter = new SettingsPresenterImpl(this);

        getPreferenceManager().setSharedPreferencesName(SettingsManagerImpl.PREFS_USER);
        addDefaultPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.settings_fragment_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);

        if (mSettingsPresenter.isUserAuthorized()) {
            mSettingsPresenter.refresh(true);
        }
    }

    @Override
    public void onPause() {
        getActivity().getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        if (p instanceof CheckBoxPreference) {
            ((CheckBoxPreference) p).setChecked(sharedPreferences.getBoolean(key, false));
        }
    }

    @Override
    public void showPreferences(boolean showUser) {
        getActivity().invalidateOptionsMenu();
        getPreferenceScreen().removeAll();
        addDefaultPreferences();
        if (showUser) addUserPreferences();
        updateAllPrefSummaries();
    }

    private void addDefaultPreferences() {
        addPreferencesFromResource(R.xml.preferences_all); // Required for getSharedPreferences()
        Preference prefAboutApp = findPreference("pref_about_app");
        prefAboutApp.setOnPreferenceClickListener(preference -> {
            showAboutApp();
            return false;
        });
    }

    private void addUserPreferences() {
        addPreferencesFromResource(R.xml.preferences_user);
        UserIdentity user = mIdentityManager.getUserIdentity();
        if (user != null && user.isGold()) {
            addPreferencesFromResource(R.xml.preferences_user_gold);
        }
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
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuTintUtils.tintAllIcons(menu, ContextCompat.getColor(getActivity(), R.color.icons));
        menu.findItem(R.id.action_refresh).setVisible(mSettingsPresenter.isRefreshable());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mSettingsPresenter.refresh(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutApp() {
        Intent intent = AboutAppActivity.getIntent(getActivity());
        startActivity(intent);
    }

    @Override
    public void showSpinner(String message) {
        if (mLoadingOverlay == null) {
            mLoadingOverlay = new ProgressDialog(getView().getContext(), R.style.ProgressDialog);
            mLoadingOverlay.setCancelable(false);
            mLoadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mLoadingOverlay.setMessage(message);
        mLoadingOverlay.show();
    }

    @Override
    public void dismissSpinner() {
        if (mLoadingOverlay != null && mLoadingOverlay.isShowing()) {
            mLoadingOverlay.dismiss();
        }
    }

    @Override
    public void showToast(@NonNull String msg) {
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
    }

    protected void showError(@NonNull String msg) {
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
    }
}
