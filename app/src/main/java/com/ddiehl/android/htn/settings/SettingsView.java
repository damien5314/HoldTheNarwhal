package com.ddiehl.android.htn.settings;

public interface SettingsView {

    void showPreferences(boolean showUser);

    void showToast(String message);

    void showSpinner();

    void dismissSpinner();
}
