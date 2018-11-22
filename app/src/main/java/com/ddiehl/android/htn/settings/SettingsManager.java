package com.ddiehl.android.htn.settings;

import com.ddiehl.android.htn.view.theme.ColorScheme;

import androidx.annotation.NonNull;
import rxreddit.model.UserSettings;

public interface SettingsManager {

    boolean hasFromRemote();

    void saveUserSettings(UserSettings settings);

    void clearUserSettings();

    String getDeviceId();

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
