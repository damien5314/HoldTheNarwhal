package com.ddiehl.android.htn.settings;

import android.content.SharedPreferences;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.managers.NetworkConnectivityManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;
import timber.log.Timber;

public class SettingsPresenter implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final SettingsView settingsView;
    private final SharedPreferences sharedPreferences;
    private final RedditService redditService;
    private final SettingsManager settingsManager;
    private final NetworkConnectivityManager networkConnectivityManager;

    private boolean isChanging = false;

    public SettingsPresenter(
            SettingsView settingsView,
            SharedPreferences sharedPreferences,
            RedditService redditService,
            SettingsManager settingsManager,
            NetworkConnectivityManager networkConnectivityManager
    ) {
        this.settingsView = settingsView;
        this.sharedPreferences = sharedPreferences;
        this.redditService = redditService;
        this.settingsManager = settingsManager;
        this.networkConnectivityManager = networkConnectivityManager;
    }

    public void attachView(SettingsView settingsView) {
        watchPreferences();
    }

    public void detachView(SettingsView settingsView) {
        unwatchPreferences();
    }

    public void refresh(boolean pullFromServer) {
        boolean showUser = settingsManager.hasFromRemote();
        settingsView.showPreferences(showUser);

        if (pullFromServer && isUserAuthorized()) {
            if (networkConnectivityManager.isConnectedToNetwork()) {
                loadServerData();
            } else {
                settingsView.showToast(R.string.error_network_unavailable);
            }
        }
    }

    void loadServerData() {
        redditService.getUserSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> settingsView.showSpinner())
                .doFinally(settingsView::dismissSpinner)
                .subscribe(
                        settings -> {
                            unwatchPreferences();
                            settingsManager.saveUserSettings(settings);
                            refresh(false);
                            watchPreferences();
                        },
                        error -> {
                            if (error instanceof IOException) {
                                settingsView.showToast(R.string.error_network_unavailable);
                            } else {
                                Timber.w(error, "Error getting user settings");
                                settingsView.showToast(R.string.error_get_user_settings);
                            }
                        }
                );
    }

    public boolean isRefreshable() {
        return !settingsManager.hasFromRemote();
    }

    public boolean isUserAuthorized() {
        return redditService.isUserAuthorized();
    }

    private void watchPreferences() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void unwatchPreferences() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (isChanging) {
            return;
        }
        isChanging = true;

        Map<String, String> changedSettings = new HashMap<>(); // Track changed keys and values

        Object p = getValueFromKey(sp, key);
        changedSettings.put(key, String.valueOf(p));

        // Force "make safe(r) for work" to be true if "over 18" is false
        boolean over18 = sp.getBoolean(SettingsManagerImpl.PREF_OVER_18, false);
        if (!over18) {
            boolean noProfanity = sp.getBoolean(SettingsManagerImpl.PREF_NO_PROFANITY, true);
            if (!noProfanity) {
                sp.edit().putBoolean(SettingsManagerImpl.PREF_NO_PROFANITY, true).apply();
                changedSettings.put(SettingsManagerImpl.PREF_NO_PROFANITY, String.valueOf(true));
            }
        }

        // Force "label nsfw" to be true if "make safe(r) for work" is true
        boolean noProfanity = sp.getBoolean(SettingsManagerImpl.PREF_NO_PROFANITY, true);
        if (noProfanity) {
            boolean labelNsfw = sp.getBoolean(SettingsManagerImpl.PREF_LABEL_NSFW, true);
            if (!labelNsfw) {
                sp.edit().putBoolean(SettingsManagerImpl.PREF_LABEL_NSFW, true).apply();
                changedSettings.put(SettingsManagerImpl.PREF_LABEL_NSFW, String.valueOf(true));
            }
        }

        if (changedSettings.size() > 0 && redditService.isUserAuthorized()) {
            // Post SettingsUpdate event with changed keys and values
            redditService.updateUserSettings(changedSettings)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> Timber.d("Settings updated successfully"),
                            error -> Timber.w(error, "Error updating settings")
                    );
        }

        if (key.equals(SettingsManagerImpl.PREF_COLOR_SCHEME)) {
            settingsView.notifyColorSchemeUpdated();
        }

        isChanging = false;
    }

    private Object getValueFromKey(SharedPreferences sp, String key) {
        return sp.getAll().get(key);
    }
}
