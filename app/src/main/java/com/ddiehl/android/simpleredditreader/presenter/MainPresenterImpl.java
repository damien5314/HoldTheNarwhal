package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.RedditIdentityManager;
import com.ddiehl.android.simpleredditreader.events.requests.UserSignOutEvent;
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

    public MainPresenterImpl(Context context, MainView view) {
        mBus = BusProvider.getInstance();
        mContext = context.getApplicationContext();

        mMainView = view;
        mIdentityManager = RedditIdentityManager.getInstance(mContext);

        mMainView.setAccount(mIdentityManager.getUserIdentity());
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        mMainView.setAccount(identity);
        mMainView.updateNavigationItems();
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
}
