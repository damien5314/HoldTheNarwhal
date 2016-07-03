package com.ddiehl.android.htn.view;

public interface SettingsView {

  void showPreferences(boolean showUser);

  void showToast(String message);

  void showSpinner(String message);

  void dismissSpinner();
}
