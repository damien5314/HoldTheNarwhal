package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.UserProfileView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.UserIdentity;

public class UserProfilePresenter extends BaseListingsPresenter
    implements LinkPresenter, CommentPresenter {

  private final UserProfileView mSummaryView;

  public UserProfilePresenter(MainView main, RedditNavigationView navigationView, UserProfileView view) {
    super(main, navigationView, view, view, view, null);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    mSummaryView = view;
  }

  public boolean isAuthenticatedUser() {
    UserIdentity authenticatedUser = mIdentityManager.getUserIdentity();
    return authenticatedUser != null
        && Utils.equals(mSummaryView.getUsernameContext(), authenticatedUser.getName());
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> {
      mSummaryView.onAuthenticatedStateChanged(isAuthenticatedUser());
//      super.onUserIdentityChanged().call(identity);
    };
  }

  @Override
  void requestPreviousData() {
    if ("summary".equals(mSummaryView.getShow())) {
      getSummaryData();
    } else {
      getListingData(false);
    }
  }

  @Override
  public void requestNextData() {
    mAnalytics.logLoadUserProfile(mSummaryView.getShow(), mSummaryView.getSort(), mSummaryView.getTimespan());
    if ("summary".equals(mSummaryView.getShow())) {
      getSummaryData();
    } else {
      getListingData(true);
    }
  }

  private void getListingData(boolean append) {
    String before = append ? null : mPrevPageListingId;
    String after = append ? mNextPageListingId : null;
    mRedditService.loadUserProfile(
        mSummaryView.getShow(), mSummaryView.getUsernameContext(),
        mSummaryView.getSort(), mSummaryView.getTimespan(),
        before, after)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
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
        .subscribe(onListingsLoaded(append), e -> {
          String message = mContext.getString(R.string.error_get_user_profile_listings);
          mMainView.showError(e, message);
        });
  }

  public void requestData() {
    refreshData();
  }

  private void getSummaryData() {
    mRedditService.getUserInfo(mSummaryView.getUsernameContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
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
            e -> {
              String message = mContext.getString(R.string.error_get_user_info);
              mMainView.showError(e, message);
            });
    mRedditService.getUserTrophies(mSummaryView.getUsernameContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mSummaryView::showTrophies,
            e -> {
              String message = mContext.getString(R.string.error_get_user_trophies);
              mMainView.showError(e, message);
            });
  }

  private Action1<UserIdentity> getFriendInfo() {
    return user -> {
      if (user.isFriend()) {
        mRedditService.getFriendInfo(user.getName())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
              UserIdentity self = mIdentityManager.getUserIdentity();
              if (self != null && self.isGold()) {
                mSummaryView.showFriendNote(response.getNote());
              }
            }, e -> {
              String message = mContext.getString(R.string.error_get_friend_info);
              mMainView.showError(e, message);
            });
      }
    };
  }

  public void addFriend() {
    mRedditService.addFriend(mSummaryView.getUsernameContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> mMainView.showSpinner(null))
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(response -> {
          mSummaryView.setFriendButtonState(true);
          UserIdentity self = mIdentityManager.getUserIdentity();
          if (self != null && self.isGold()) {
            mSummaryView.showFriendNote("");
          }
          mMainView.showToast(R.string.user_friend_add_confirm);
        }, e -> {
          String message = mContext.getString(R.string.user_friend_add_error);
          mMainView.showError(e, message);
        });
  }

  public void deleteFriend() {
    mRedditService.deleteFriend(mSummaryView.getUsernameContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> mMainView.showSpinner(null))
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(response -> {
          mSummaryView.setFriendButtonState(false);
          mSummaryView.hideFriendNote();
          mMainView.showToast(R.string.user_friend_delete_confirm);
        }, e -> {
          String message = mContext.getString(R.string.user_friend_delete_error);
          mMainView.showError(e, message);
        });
  }

  public void saveFriendNote(@NonNull String note) {
    // Note must be non-empty for a positive response
    if (TextUtils.isEmpty(note)) mMainView.showToast(R.string.user_friend_empty_note);
    else {
      mMainView.showSpinner(null);
      mRedditService.saveFriendNote(mSummaryView.getUsernameContext(), note)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnTerminate(mMainView::dismissSpinner)
          .subscribe(r -> mMainView.showToast(R.string.user_friend_note_save_confirm),
              e -> {
                String message = mContext.getString(R.string.user_friend_note_save_error);
                mMainView.showError(e, message);
              });
    }
  }
}
