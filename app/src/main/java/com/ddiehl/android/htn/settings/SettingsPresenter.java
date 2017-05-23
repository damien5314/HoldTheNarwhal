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

    @Inject Context mContext;
    @Inject RedditService mRedditService;
    @Inject IdentityManager mIdentityManager;
    @Inject SettingsManager mSettingsManager;

    private final SettingsView mSettingsView;

    public SettingsPresenter(SettingsView settingsView) {
        mSettingsView = settingsView;
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    public void refresh(boolean pullFromServer) {
        boolean showUser = mSettingsManager.hasFromRemote();
        mSettingsView.showPreferences(showUser);

        if (pullFromServer) {
            if (AndroidUtils.isConnectedToNetwork(mContext)) {
                loadServerData();
            } else {
                String message = mContext.getString(R.string.error_network_unavailable);
                mSettingsView.showToast(message);
            }
        }
    }

    void loadServerData() {
        mRedditService.getUserSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mSettingsView.showSpinner())
                .doOnDispose(mSettingsView::dismissSpinner)
                .doOnNext(mSettingsManager::saveUserSettings)
                .subscribe(
                        settings -> refresh(false),
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mSettingsView.showToast(message);
                            } else {
                                Timber.w(error, "Error getting user settings");
                                String message = mContext.getString(R.string.error_get_user_settings);
                                mSettingsView.showToast(message);
                            }
                        }
                );
    }

    public boolean isRefreshable() {
        return !mSettingsManager.hasFromRemote();
    }

    public boolean isUserAuthorized() {
        return mRedditService.isUserAuthorized();
    }
}
