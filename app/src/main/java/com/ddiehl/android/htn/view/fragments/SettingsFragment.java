package com.ddiehl.android.htn.view.fragments;

import android.content.Context;
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

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManagerImpl;
import com.ddiehl.android.htn.presenter.SettingsPresenter;
import com.ddiehl.android.htn.presenter.SettingsPresenterImpl;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.SettingsView;
import com.ddiehl.reddit.identity.UserIdentity;

import java.io.IOException;
import java.io.InputStream;

public class SettingsFragment extends PreferenceFragment
    implements SettingsView, SharedPreferences.OnSharedPreferenceChangeListener {
  private SettingsPresenter mSettingsPresenter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
    mSettingsPresenter = new SettingsPresenterImpl((MainView) getActivity(), this);
    getPreferenceManager().setSharedPreferencesName(SettingsManagerImpl.PREFS_USER);
    addDefaultPreferences();
  }

  @Override
  public void onStart() {
    super.onStart();
    getActivity().setTitle(R.string.settings_fragment_title);
  }

  @Override
  public void onResume() {
    super.onResume();
    mSettingsPresenter.onResume();
    getActivity().getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    getActivity().getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(this);
    mSettingsPresenter.onPause();
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
    UserIdentity user = HoldTheNarwhal.getIdentityManager().getUserIdentity();
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
    inflater.inflate(R.menu.settings_menu, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
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

  private void showAboutAppHtml() {
    getFragmentManager().beginTransaction()
        .replace(R.id.fragment_container,
            WebViewFragment.newInstance("file:///android_asset/htn_about_app.html"))
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void showAboutApp() {
    InputStream in_s;
    try {
      in_s = getActivity().getAssets().open("htn_about_app.md");
      getFragmentManager().beginTransaction()
          .replace(R.id.fragment_container, AboutAppFragment.newInstance(in_s))
          .addToBackStack(null)
          .commit();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
