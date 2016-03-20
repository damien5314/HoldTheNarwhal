package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.UserProfileView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.UserIdentity;

public class UserProfilePresenter extends BaseListingsPresenter
    implements LinkPresenter, CommentPresenter {
  private UserProfileView mSummaryView;

  public UserProfilePresenter(
      MainView main, ListingsView listingsView, LinkView linkView,
      CommentView commentView, UserProfileView userProfileView,
      String show, String username, String sort, String timespan) {
    super(main, listingsView, linkView, commentView, userProfileView,
        null, show, username, null, sort, timespan);
    mSummaryView = userProfileView;
  }

  @Override
  public void onResume() {
    super.onResume();
    mSummaryView.refreshTabs(isAuthenticatedUser());
    mSummaryView.selectTab(mShow);
    mMainView.setTitle(
        String.format(mContext.getString(R.string.username_formatter), mUsernameContext));
  }

  private boolean isAuthenticatedUser() {
    UserIdentity authenticatedUser = mIdentityManager.getUserIdentity();
    return authenticatedUser != null
        && Utils.equals(mUsernameContext, authenticatedUser.getName());
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> {
      if (isInAuthenticatedView()) {
        mShow = "summary";
        mUserProfileView.selectTab(mShow);
      }
      mUserProfileView.refreshTabs(isAuthenticatedUser());
      super.onUserIdentityChanged().call(identity);
    };
  }

  @Override
  void requestPreviousData() {
    mAnalytics.logLoadUserProfile(mShow, mSort, mTimespan);
    if (mShow.equals("summary")) {
      getSummaryData();
    } else {
      getListingData(false);
    }
  }

  @Override
  public void requestNextData() {
    mAnalytics.logLoadUserProfile(mShow, mSort, mTimespan);
    if (mShow.equals("summary")) {
      getSummaryData();
    } else {
      getListingData(true);
    }
  }

  private void getListingData(boolean append) {
    mRedditService.loadUserProfile(mShow, mUsernameContext, mSort, mTimespan,
        append ? null : mPrevPageListingId,
        append ? mNextPageListingId : null)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> {
          mMainView.showSpinner(null);
          if (append) mNextRequested = true;
          else mBeforeRequested = true;
        })
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          if (append) mNextRequested = false;
          else mBeforeRequested = false;
        })
        .subscribe(onListingsLoaded(append),
            e -> mMainView.showError(e, R.string.error_get_user_profile_listings));
  }

  public void requestData(String show) {
    mShow = show;
    refreshData();
  }

  private void getSummaryData() {
    mRedditService.getUserInfo(mUsernameContext)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> {
          mMainView.showSpinner(null);
          mNextRequested = true;
        })
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          mNextRequested = false;
        })
        .doOnNext(getFriendInfo())
        .subscribe(mSummaryView::showUserInfo,
            e -> mMainView.showError(e, R.string.error_get_user_info));
    mRedditService.getUserTrophies(mUsernameContext)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(mSummaryView::showTrophies,
            e -> mMainView.showError(e, R.string.error_get_user_trophies));
  }

  private Action1<UserIdentity> getFriendInfo() {
    return user -> {
      if (user.isFriend()) {
        mRedditService.getFriendInfo(user.getName())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
              UserIdentity self = HoldTheNarwhal.getIdentityManager().getUserIdentity();
              if (self != null && self.isGold()) {
                mSummaryView.showFriendNote(response.getNote());
              }
            }, e -> mMainView.showError(e, R.string.error_get_friend_info));
      }
    };
  }

  public void addFriend() {
    mRedditService.addFriend(mUsernameContext)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(response -> {
          mSummaryView.setFriendButtonState(true);
          UserIdentity self = HoldTheNarwhal.getIdentityManager().getUserIdentity();
          if (self != null && self.isGold()) {
            mSummaryView.showFriendNote("");
          }
          mMainView.showToast(R.string.user_friend_add_confirm);
        }, e -> mMainView.showError(e, R.string.user_friend_add_error));
  }

  public void deleteFriend() {
    mRedditService.deleteFriend(mUsernameContext)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(response -> {
          mSummaryView.setFriendButtonState(false);
          mSummaryView.hideFriendNote();
          mMainView.showToast(R.string.user_friend_delete_confirm);
        }, e -> mMainView.showError(e, R.string.user_friend_delete_error));
  }

  public void saveFriendNote(@NonNull String note) {
    // Note must be non-empty for a positive response
    if (TextUtils.isEmpty(note)) mMainView.showToast(R.string.user_friend_empty_note);
    else {
      mMainView.showSpinner(null);
      mRedditService.saveFriendNote(mUsernameContext, note)
          .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
          .doOnTerminate(mMainView::dismissSpinner)
          .subscribe(r -> mMainView.showToast(R.string.user_friend_note_save_confirm),
              e -> mMainView.showError(e, R.string.user_friend_note_save_error));
    }
  }

  private boolean isInAuthenticatedView() {
    return mShow.equals("upvoted")
        || mShow.equals("downvoted")
        || mShow.equals("hidden")
        || mShow.equals("saved");
  }
}
