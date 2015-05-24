package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.IdentityBroker;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.responses.SavedUserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserSignedOutEvent;
import com.ddiehl.android.simpleredditreader.io.RedditService;
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
        mIdentityBroker = new IdentityBroker(mContext);

        RedditPreferences prefs = RedditPreferences.getInstance(mContext);
        mBus.register(prefs);

        RedditService authProxy = RedditServiceAuth.getInstance(mContext);
        mBus.register(authProxy);

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
    }

    @Subscribe
    public void onUserSignedOut(UserSignedOutEvent event) {
        mMainView.setAccount(null);
    }
}
