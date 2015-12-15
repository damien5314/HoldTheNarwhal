package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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
        .subscribe(mSummaryView::showUserInfo, mMainView::showError);
    mRedditService.getUserTrophies(mUsernameContext)
        .subscribe(mSummaryView::showTrophies, mMainView::showError);
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
            }, mMainView::showError);
      }
    };
  }

  public void addFriend() {
    mRedditService.addFriend(mUsernameContext)
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(response -> {
          mSummaryView.setFriendButtonState(true);
          UserIdentity self = HoldTheNarwhal.getIdentityManager().getUserIdentity();
          if (self != null && self.isGold()) {
            mSummaryView.showFriendNote("");
          }
          mMainView.showToast(R.string.user_friend_add_confirm);
        }, e -> mMainView.showToast(R.string.user_friend_add_error));
  }

  public void deleteFriend() {
    mRedditService.deleteFriend(mUsernameContext)
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(response -> {
          mSummaryView.setFriendButtonState(false);
          mSummaryView.hideFriendNote();
          mMainView.showToast(R.string.user_friend_delete_confirm);
        }, e -> mMainView.showToast(R.string.user_friend_delete_error));
  }

  public void saveFriendNote(@NonNull String note) {
    // Note must be non-empty for a positive response
    if (TextUtils.isEmpty(note)) mMainView.showToast(R.string.user_friend_empty_note);
    else {
      mMainView.showSpinner(null);
      mRedditService.saveFriendNote(mUsernameContext, note)
          .doOnTerminate(mMainView::dismissSpinner)
          .subscribe(r -> mMainView.showToast(R.string.user_friend_note_save_confirm),
              e -> mMainView.showToast(R.string.user_friend_note_save_error));
    }
  }

  private void getListingData() {
    mRedditService.loadUserProfile(mShow, mUsernameContext, mSort, mTimespan, mNextPageListingId)
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          mListingsRequested = false;
        })
        .subscribe(onListingsLoaded(), mMainView::showError);
  }

  public void requestData(String show) {
    mShow = show;
    refreshData();
  }
}
