package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditIdentityManager;
import com.ddiehl.android.simpleredditreader.events.requests.UserSignOutEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainPresenterImpl implements MainPresenter {

    private Bus mBus;
    private Context mContext;

    private MainView mMainView;
    private RedditIdentityManager mIdentityManager;
    private String mUsernameContext;

    public MainPresenterImpl(Context context, MainView view) {
        mBus = BusProvider.getInstance();
        mContext = context.getApplicationContext();

        mMainView = view;
        mIdentityManager = RedditIdentityManager.getInstance(mContext);
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        mMainView.updateUserIdentity();
    }

    @Subscribe
    public void onUserIdentityRetrieved(UserIdentityRetrievedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        mMainView.showToast(String.format(mContext.getString(R.string.welcome_user), identity.getName()));
    }

    @Subscribe
    public void onUserSignOut(UserSignOutEvent event) {
        mMainView.showToast(R.string.user_signed_out);
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
