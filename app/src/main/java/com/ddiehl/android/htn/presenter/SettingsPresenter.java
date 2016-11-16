package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.SettingsView;

import java.io.IOException;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
                getData();
            } else {
                String message = mContext.getString(R.string.error_network_unavailable);
                mSettingsView.showToast(message);
            }
        }
    }

    private void getData() {
        mSettingsView.showSpinner(null);
        mRedditService.getUserSettings()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(mSettingsView::dismissSpinner)
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
        return mSettingsManager.hasFromRemote();
    }

    public boolean isUserAuthorized() {
        return mRedditService.isUserAuthorized();
    }
}
