package com.ddiehl.android.htn.view;

import android.support.annotation.StringRes;

public interface SettingsView {

  void showPreferences(boolean showUser);

  void showToast(@StringRes int messageResId);

  void showError(Throwable e, @StringRes int messageResId);

  void showSpinner(String message);

  void dismissSpinner();
}
