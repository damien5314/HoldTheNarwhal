/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.util.Log;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.Analytics;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.RetrofitError;

public class MainPresenterImpl implements MainPresenter {

    private Bus mBus;
    private Context mContext;

    private MainView mMainView;
    private IdentityManager mIdentityManager;
    private String mUsernameContext;
    private String mLastUser;

    public MainPresenterImpl(Context context, MainView view) {
        mBus = BusProvider.getInstance();
        mContext = context.getApplicationContext();

        mMainView = view;
        mIdentityManager = IdentityManager.getInstance(mContext);
    }

    @Subscribe
    public void onNetworkError(RetrofitError error) {
        Log.e("HTN", "RetrofitError: " + error.getKind().toString());
        Log.e("HTN", Log.getStackTraceString(error));
        mMainView.showToast(BaseUtils.getFriendlyError(mContext, error));
        Analytics.getInstance().logApiError(error);
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        mMainView.updateUserIdentity();

        UserIdentity id = event.getUserIdentity();
        if (id != null && !id.getName().equals(mLastUser)) {
            mMainView.showToast(String.format(mContext.getString(R.string.welcome_user), id.getName()));
//            mIdentityManager.saveUserIdentity(id);
        }
    }

    @Subscribe
    public void onUserSignOut(UserSignOutEvent event) {
        mMainView.showToast(R.string.user_signed_out);
        mLastUser = null;
    }

    @Override
    public UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
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
}
