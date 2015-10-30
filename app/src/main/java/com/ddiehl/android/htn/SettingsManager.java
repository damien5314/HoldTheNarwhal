package com.ddiehl.android.htn;

import android.content.SharedPreferences;

import com.ddiehl.android.htn.events.responses.UserSettingsRetrievedEvent;
import com.ddiehl.reddit.identity.UserSettings;
import com.squareup.otto.Subscribe;

public interface SettingsManager extends SharedPreferences.OnSharedPreferenceChangeListener {
    boolean hasFromRemote();

    @Subscribe
    @SuppressWarnings("unused")
    void onUserSettingsRetrieved(UserSettingsRetrievedEvent event);

    @Override
    void onSharedPreferenceChanged(SharedPreferences sp, String key);

    void saveUserSettings(UserSettings settings);

    void clearUserSettings();

    String getDeviceId();

    boolean areAnalyticsEnabled();

    void setAnalyticsEnabled(boolean b);

    boolean askedForAnalytics();

    void setAskedForAnalytics(boolean b);

    boolean getAdsEnabled();

    String getCommentSort();

    void saveCommentSort(String pref);

    Integer getMinCommentScore();

    boolean getShowControversiality();

    void setOver18(boolean b);
}
