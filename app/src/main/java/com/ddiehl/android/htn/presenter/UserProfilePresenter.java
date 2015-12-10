package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.events.requests.LoadUserProfileListingEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.UserProfileSummaryView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Subscribe;

import rx.functions.Action1;

public class UserProfilePresenter extends AbsListingsPresenter {
    private UserProfileSummaryView mSummaryView;

    public UserProfilePresenter(
            MainView main, ListingsView view, UserProfileSummaryView view2,
            String show, String username, String sort, String timespan) {
        super(main, view, show, username, null, sort, timespan);
        mSummaryView = view2;
    }

    @Override
    public void requestData() {
        if (mShow.equals("summary")) {
            getSummaryData();
            // TODO: Analytics event for user profile summary screen
        } else {
            getListingData();
            mAnalytics.logLoadUserProfile(mShow, mSort, mTimespan);
        }
    }

    private void getSummaryData() {
        mRedditService.getUserInfo(mUsernameContext)
                .doOnTerminate(mMainView::dismissSpinner)
                .doOnNext(getFriendInfo())
                .subscribe(mSummaryView::showUserInfo);
        mRedditService.getUserTrophies(mUsernameContext)
                .subscribe(mSummaryView::showTrophies);
    }

    private Action1<UserIdentity> getFriendInfo() {
        return user -> {
            if (user.isFriend()) {
                mRedditService.getFriendInfo(user.getName())
                        .subscribe(mSummaryView::showFriendInfo);
            }
        };
    }

    private void getListingData() {
        mBus.post(new LoadUserProfileListingEvent(mShow, mUsernameContext, mSort, mTimespan, mNextPageListingId));
    }

    public void requestData(String show) {
        mShow = show;
        refreshData();
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
