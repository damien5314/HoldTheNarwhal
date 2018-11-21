package com.ddiehl.android.htn.settings;

import android.content.Context;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.utils.AndroidUtils;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;
import timber.log.Timber;

public class SettingsPresenter {

    @Inject Context context;
    @Inject RedditService redditService;
    @Inject IdentityManager identityManager;
    @Inject SettingsManager settingsManager;

    private final SettingsView settingsView;

    public SettingsPresenter(SettingsView settingsView) {
        this.settingsView = settingsView;
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    public void refresh(boolean pullFromServer) {
        boolean showUser = settingsManager.hasFromRemote();
        settingsView.showPreferences(showUser);

        if (pullFromServer && isUserAuthorized()) {
            if (AndroidUtils.isConnectedToNetwork(context)) {
                loadServerData();
            } else {
                String message = context.getString(R.string.error_network_unavailable);
                settingsView.showToast(message);
            }
        }
    }

    void loadServerData() {
        redditService.getUserSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> settingsView.showSpinner())
                .doFinally(settingsView::dismissSpinner)
                .doOnNext(settingsManager::saveUserSettings)
                .subscribe(
                        settings -> refresh(false),
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                settingsView.showToast(message);
                            } else {
                                Timber.w(error, "Error getting user settings");
                                String message = context.getString(R.string.error_get_user_settings);
                                settingsView.showToast(message);
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
}
