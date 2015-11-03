package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.events.requests.LoadUserProfileListingEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileSummaryEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.events.responses.UserInfoLoadedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.squareup.otto.Subscribe;

public class UserProfilePresenter extends AbsListingsPresenter {

    public UserProfilePresenter(
            MainView main, ListingsView view, String show, String username,
            String sort, String timespan) {
        super(main, view, show, username, null, sort, timespan);
    }

    @Override
    public void requestData() {
        if (mShow.equals("summary")) {
            mBus.post(new LoadUserProfileSummaryEvent(mUsernameContext));
            // TODO: Analytics event for user profile summary screen
        } else {
            mBus.post(new LoadUserProfileListingEvent(mShow, mUsernameContext, mSort, mTimespan, mNextPageListingId));
            mAnalytics.logLoadUserProfile(mShow, mSort, mTimespan);
        }
    }

    public void requestData(String show) {
        mShow = show;
        refreshData();
    }

    @Subscribe
    public void onUserInfoLoaded(UserInfoLoadedEvent event) {
        super.onUserInfoLoaded(event);
    }

    @Subscribe @Override
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        super.onUserIdentitySaved(event);
        ((UserProfileFragment) mListingsView).updateUserProfileTabs();
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
