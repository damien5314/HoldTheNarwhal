package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.IdentityBroker;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.requests.UserSignOutEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SavedUserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.io.RedditServiceAuth;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainPresenterImpl implements MainPresenter {

    private Bus mBus;
    private Context mContext;

    private MainView mMainView;
    private IdentityBroker mIdentityBroker;

    public MainPresenterImpl(Context context, MainView view) {
        mBus = BusProvider.getInstance();
        mContext = context.getApplicationContext();

        mMainView = view;
        mIdentityBroker = IdentityBroker.getInstance(mContext);

        mMainView.setAccount(mIdentityBroker.getUserIdentity());
    }

    @Subscribe
    public void onSavedUserIdentityRetrieved(SavedUserIdentityRetrievedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        mMainView.setAccount(identity);
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        mMainView.setAccount(identity);
        mMainView.updateNavigationItems();
    }

    @Override
    public void presentLoginView() {
        mMainView.closeNavigationDrawer();
        mMainView.openWebViewForURL(RedditServiceAuth.AUTHORIZATION_URL);
    }

    @Override
    public void showSubreddit(String subreddit) {
        mMainView.closeNavigationDrawer();
        mMainView.showSubreddit(subreddit);
    }

    @Override
    public UserIdentity getAuthenticatedUser() {
        return mIdentityBroker.getUserIdentity();
    }

    @Override
    public void signOutUser() {
        mBus.post(new UserSignOutEvent());
    }

    @Override
    public void showUserProfile(String userId) {
        mMainView.showToast(R.string.implementation_pending);
    }

    @Override
    public void showUserSubreddits() {
        mMainView.showToast(R.string.implementation_pending);
    }
}
