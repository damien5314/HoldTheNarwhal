package com.ddiehl.android.htn.settings;

import androidx.annotation.StringRes;

public interface SettingsView {

    void showPreferences(boolean showUser);

    void notifyThemeUpdated();

    void showToast(@StringRes int messageResId);

    void showError(@StringRes int messageResId);

    void showSpinner();

    void dismissSpinner();
}
