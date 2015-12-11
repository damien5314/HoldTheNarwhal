package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.UserProfileView;
import com.ddiehl.reddit.identity.UserIdentity;

import rx.functions.Action1;

public class UserProfilePresenter extends AbsListingsPresenter {
    private UserProfileView mSummaryView;

    public UserProfilePresenter(
            MainView main, ListingsView view, UserProfileView view2,
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
                .doOnTerminate(() -> {
                    mMainView.dismissSpinner();
                    mListingsRequested = false;
                })
                .doOnNext(getFriendInfo())
                .subscribe(mSummaryView::showUserInfo);
        mRedditService.getUserTrophies(mUsernameContext)
                .subscribe(mSummaryView::showTrophies);
    }

    private Action1<UserIdentity> getFriendInfo() {
        return user -> {
            if (user.isFriend()) {
                mRedditService.getFriendInfo(user.getName())
                        .subscribe(response -> {
                            UserIdentity self = HoldTheNarwhal.getIdentityManager().getUserIdentity();
                            if (self != null && self.isGold()) {
                                mSummaryView.showFriendNote(response.getNote());
                            }
                        });
            }
        };
    }

    public void addFriend() {
        mRedditService.addFriend(mUsernameContext)
                .doOnTerminate(mMainView::dismissSpinner)
                .doOnError(error -> mMainView.showToast(R.string.user_friend_add_error))
                .subscribe(response -> {
                    mSummaryView.setFriendButtonState(true);
                    UserIdentity self = HoldTheNarwhal.getIdentityManager().getUserIdentity();
                    if (self != null && self.isGold()) {
                        mSummaryView.showFriendNote("");
                    }
                });
    }

    public void deleteFriend() {
        mRedditService.deleteFriend(mUsernameContext)
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(response -> {
                    mSummaryView.setFriendButtonState(false);
                    mSummaryView.hideFriendNote();
                });
    }

    // Note must be non-empty for a positive response (API bug?)
    public void saveFriendNote(@NonNull String note) {
        mRedditService.saveFriendNote(mUsernameContext, note)
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(response -> {},
                        error -> mMainView.showToast(R.string.user_friend_add_error));
    }

    private void getListingData() {
        mRedditService.loadUserProfile(mShow, mUsernameContext, mSort, mTimespan, mNextPageListingId)
                .subscribe(onListingsLoaded());
    }

    public void requestData(String show) {
        mShow = show;
        refreshData();
    }
}
