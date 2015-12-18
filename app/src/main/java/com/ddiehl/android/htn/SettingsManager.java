package com.ddiehl.android.htn;

import android.content.SharedPreferences;

import com.ddiehl.reddit.identity.UserSettings;

public interface SettingsManager extends SharedPreferences.OnSharedPreferenceChangeListener {
  boolean hasFromRemote();
  void saveUserSettings(UserSettings settings);
  void clearUserSettings();
  String getDeviceId();
  boolean areAnalyticsEnabled();
  void setAnalyticsEnabled(boolean b);
  boolean customTabsEnabled();
  boolean askedForAnalytics();
  void setAskedForAnalytics(boolean b);
  boolean getAdsEnabled();
  String getCommentSort();
  void saveCommentSort(String pref);
  Integer getMinCommentScore();
  boolean getShowControversiality();
  boolean getOver18();
  void setOver18(boolean b);
  boolean getNoProfanity();
  boolean getLabelNsfw();
}
