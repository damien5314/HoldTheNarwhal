/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.Analytics;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Response;

public class MainPresenterImpl implements MainPresenter {

    private Bus mBus = BusProvider.getInstance();
    private Context mContext;

    private MainView mMainView;
    private IdentityManager mIdentityManager;
    private SettingsManager mSettingsManager;
    private Analytics mAnalytics = Analytics.getInstance();
    private String mUsernameContext;

    public MainPresenterImpl(Context context, MainView view) {
        mContext = context.getApplicationContext();

        mMainView = view;
        mIdentityManager = IdentityManager.getInstance(mContext);
        mSettingsManager = SettingsManager.getInstance(mContext);

        // Configure analytics
        mAnalytics.initializeFlurry(mContext);
    }

    @Override
    public void onResume() {
        mBus.register(this);
        UserIdentity user = getAuthorizedUser();
        mMainView.updateUserIdentity(user);
        mAnalytics.setUserIdentity(user == null ? null : user.getName());

        boolean b = user != null && user.getName() != null;
        mMainView.updateNavigationItems(b);

        if (!showAnalyticsRequestIfNeverShown()) {
            mAnalytics.startSession();
            mMainView.showSubredditIfEmpty(null);
        }
    }

    @Override
    public void onPause() {
        mAnalytics.endSession();
        mBus.unregister(this);
    }

    @Override
    public void signOutUser() {
        mMainView.closeNavigationDrawer();
        mBus.post(new UserSignOutEvent());
    }

    @Override
    public String getUsernameContext() {
        return mUsernameContext;
    }

    @Override
    public void setUsernameContext(String username) {
        mUsernameContext = username;
    }

    private boolean showAnalyticsRequestIfNeverShown() {
        if (!mSettingsManager.askedForAnalytics()) {
            mMainView.showAnalyticsRequestDialog();
            return true;
        }
        return false;
    }

    @Override
    public void onAnalyticsAccepted() {
        mSettingsManager.setAskedForAnalytics(true);
        mSettingsManager.setAnalyticsEnabled(true);
        mAnalytics.startSession();
        mMainView.showSubredditIfEmpty(null);
    }

    @Override
    public void onAnalyticsDeclined() {
        mSettingsManager.setAskedForAnalytics(true);
        mSettingsManager.setAnalyticsEnabled(false);
        mAnalytics.endSession();
        mMainView.showSubredditIfEmpty(null);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onNetworkError(Response error) {
        Logger.e("Retrofit Error: " + error.raw().message());
//        Log.e("HTN", Log.getStackTraceString(error));
        mMainView.showToast(BaseUtils.getFriendlyError(mContext, error));
        Analytics.getInstance().logApiError(error);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        UserIdentity user = event.getUserIdentity();
        mMainView.updateUserIdentity(user);
        if (user != null) {
            // FIXME Ensure we only show this when the user changes
            String name = user.getName();
            mMainView.showToast(String.format(mContext.getString(R.string.welcome_user), name));
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserSignOut(UserSignOutEvent event) {
        mMainView.showToast(R.string.user_signed_out);
    }

    private UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }
}
