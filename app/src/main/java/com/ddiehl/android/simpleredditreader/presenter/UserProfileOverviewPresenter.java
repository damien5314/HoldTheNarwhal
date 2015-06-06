package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.requests.LoadUserOverviewEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;

public class UserProfileOverviewPresenter extends AbsListingsPresenter {

    public UserProfileOverviewPresenter(Context context, ListingsView view, String username) {
        super(context, view, username, null, null, null, null, null);
    }

    @Override
    public void refreshData() {
        if (mListingsRequested)
            return;

        mListings.clear();
        mListingsView.listingsUpdated();
        getMoreData();
    }

    @Override
    public void getMoreData() {
        if (mListingsRequested)
            return;

        mListingsRequested = true;
        mListingsView.showSpinner(R.string.spinner_getting_submissions);
        String after = mListings == null || mListings.size() == 0
                ? null : mListings.get(mListings.size() - 1).getName();
        mBus.post(new LoadUserOverviewEvent(mUsername, mSort, mTimespan, after));
    }
}
