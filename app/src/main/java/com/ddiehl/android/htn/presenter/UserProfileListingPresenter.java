/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.events.requests.LoadUserProfileListingEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.fragments.UserProfileListingFragment;
import com.squareup.otto.Subscribe;

public class UserProfileListingPresenter extends AbsListingsPresenter {

    public UserProfileListingPresenter(Context context, ListingsView view, String show,
                                       String username, String sort, String timespan) {
        super(context, view, show, username, null, sort, timespan);
    }

    @Override
    public void requestData() {
        mBus.post(new LoadUserProfileListingEvent(mShow, mUsernameContext, mSort, mTimespan, mNextPageListingId));
    }

    public void requestData(String show) {
        mShow = show;
        refreshData();
    }

    @Subscribe @Override
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        super.onUserIdentitySaved(event);
        ((UserProfileListingFragment) mListingsView).updateUserProfileTabs();
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
