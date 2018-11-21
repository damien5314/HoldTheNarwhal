package com.ddiehl.android.htn.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.utils.MenuTintUtils;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import rxreddit.model.UserIdentity;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment
        implements SettingsView, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Inject IdentityManager mIdentityManager;
    SettingsPresenter settingsPresenter;

    ProgressDialog loadingOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("Showing settings");
        HoldTheNarwhal.getApplicationComponent().inject(this);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        settingsPresenter = new SettingsPresenter(this);

        getPreferenceManager().setSharedPreferencesName(SettingsManagerImpl.PREFS_USER);
        addDefaultPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            final int bgColor = ThemeUtilsKt.getColorFromAttr(getActivity(), R.attr.windowBackgroundColorNeutral);
            view.setBackgroundColor(bgColor);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.settings_fragment_title);
        getActivity().getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);

        if (settingsPresenter.isUserAuthorized()) {
            boolean pullFromServer = settingsPresenter.isRefreshable();
            settingsPresenter.refresh(pullFromServer);
        }

        updateAllPrefSummaries();
    }

    @Override
    public void onStop() {
        getActivity().getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
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
        MenuTintUtils.tintAllIcons(menu, ThemeUtilsKt.getColorFromAttr(getActivity(), R.attr.iconColor));
        menu.findItem(R.id.action_refresh).setVisible(settingsPresenter.isRefreshable());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                settingsPresenter.refresh(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSpinner() {
        if (loadingOverlay == null) {
            loadingOverlay = new ProgressDialog(getView().getContext(), R.style.ProgressDialog);
            loadingOverlay.setCancelable(false);
            loadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
//        loadingOverlay.setMessage(message);
        loadingOverlay.show();
    }

    @Override
    public void dismissSpinner() {
        if (loadingOverlay != null && loadingOverlay.isShowing()) {
            loadingOverlay.dismiss();
        }
    }

    @Override
    public void showToast(@NotNull String msg) {
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
    }

    protected void showError(@NotNull String msg) {
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
    }
}
