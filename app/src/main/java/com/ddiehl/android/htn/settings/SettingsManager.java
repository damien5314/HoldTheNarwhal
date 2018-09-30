package com.ddiehl.android.htn.settings;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.view.theme.ColorScheme;

import rxreddit.model.UserSettings;

public interface SettingsManager
        extends SharedPreferences.OnSharedPreferenceChangeListener {

    boolean hasFromRemote();

    void saveUserSettings(UserSettings settings);

    void clearUserSettings();

    String getDeviceId();

    boolean areAnalyticsEnabled();

    void setAnalyticsEnabled(boolean b);

    String getFont();

    boolean askedForAnalytics();

    void setAskedForAnalytics(boolean b);

    String getCommentSort();

    void saveCommentSort(String pref);

    Integer getMinCommentScore();

    boolean getShowControversiality();

    boolean getOver18();

    void setOver18(boolean b);

    boolean getNoProfanity();

    boolean getLabelNsfw();

    ColorScheme getColorScheme();

    void setColorScheme(@NonNull ColorScheme night);
}
