package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.SettingsView;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;

public class SettingsPresenterImpl implements SettingsPresenter {

    @Inject protected Context mApplicationContext;
    @Inject protected RedditService mRedditService;
    @Inject protected IdentityManager mIdentityManager;
    @Inject protected SettingsManager mSettingsManager;

    private final SettingsView mSettingsView;

    public SettingsPresenterImpl(SettingsView settingsView) {
        mSettingsView = settingsView;
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    @Override
    public void refresh(boolean pullFromServer) {
        boolean showUser = mSettingsManager.hasFromRemote();
        mSettingsView.showPreferences(showUser);
        if (pullFromServer) {
            if (AndroidUtils.isConnectedToNetwork(mApplicationContext)) {
                getData();
            } else {
                String message = mApplicationContext.getString(R.string.error_network_unavailable);
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
                        e -> {
                            String message = mApplicationContext.getString(R.string.error_get_user_settings);
                            mSettingsView.showToast(message);
                        });
    }

    @Override
    public boolean isRefreshable() {
        return mSettingsManager.hasFromRemote();
    }

    @Override
    public boolean isUserAuthorized() {
        return mRedditService.isUserAuthorized();
    }
}
