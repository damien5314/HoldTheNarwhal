package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.squareup.otto.Subscribe;

public class UserProfileCommentsPresenter extends AbsListingsPresenter {

    public UserProfileCommentsPresenter(Context context, ListingsView view, String username,
                                        String sort, String timespan) {
        super(context, view, username, null, null, null, sort, timespan);
    }

    @Override
    public void getMoreData() {
        super.getMoreData();
        String after = mListings == null || mListings.size() == 0 ?
                null : mListings.get(mListings.size() - 1).getName();
        mBus.post(new LoadUserProfileEvent("comments", mUsername, mSort, mTimespan, after));
    }

    @Subscribe @Override
    public void onListingsLoaded(ListingsLoadedEvent event) {
        super.onListingsLoaded(event);
    }

    @Subscribe @Override
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        super.onVoteSubmitted(event);
    }

    @Subscribe @Override
    public void onListingSaved(SaveSubmittedEvent event) {
        super.onListingSaved(event);
    }

    @Subscribe @Override
    public void onListingHidden(HideSubmittedEvent event) {
        super.onListingHidden(event);
    }
}
