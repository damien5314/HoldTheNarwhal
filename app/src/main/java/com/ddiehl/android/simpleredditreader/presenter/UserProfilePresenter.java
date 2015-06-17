package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.squareup.otto.Subscribe;

public class UserProfilePresenter extends AbsListingsPresenter {

    public UserProfilePresenter(Context context, ListingsView view, String show, String username,
                                String sort, String timespan) {
        super(context, view, show, username, null, null, null, sort, timespan);
    }

    @Override
    public void requestData() {
        mBus.post(new LoadUserProfileEvent(mShow, mUsernameContext, mSort, mTimespan, mNextPageListingId));
    }

    public void requestData(String show) {
        mShow = show;
        refreshData();
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
