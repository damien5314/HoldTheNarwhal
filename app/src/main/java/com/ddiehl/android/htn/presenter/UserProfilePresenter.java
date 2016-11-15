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
import timber.log.Timber;

public class UserProfilePresenter extends BaseListingsPresenter {

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
                    mMainView.showSpinner();
                    if (append) mNextRequested = true;
                    else mBeforeRequested = true;
                })
                .doOnTerminate(() -> {
                    mMainView.dismissSpinner();
                    if (append) mNextRequested = false;
                    else mBeforeRequested = false;
                })
                .subscribe(
                        onListingsLoaded(append),
                        error -> {
                            Timber.w(error, "Error loading profile listings");
                            String message = mContext.getString(R.string.error_get_user_profile_listings);
                            mMainView.showError(error, message);
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
                    mMainView.showSpinner();
                    mNextRequested = true;
                })
                .doOnTerminate(() -> {
                    mMainView.dismissSpinner();
                    mNextRequested = false;
                })
                .doOnNext(getFriendInfo())
                .subscribe(
                        mSummaryView::showUserInfo,
                        error -> {
                            Timber.w(error, "Error loading friend info");
                            String message = mContext.getString(R.string.error_get_user_info);
                            mMainView.showError(error, message);
                        }
                );
        mRedditService.getUserTrophies(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mSummaryView::showTrophies,
                        error -> {
                            Timber.w(error, "Error loading user trophies");
                            String message = mContext.getString(R.string.error_get_user_trophies);
                            mMainView.showError(error, message);
                        }
                );
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
                        }, error -> {
                            Timber.w(error, "Error getting friend info");
                            String message = mContext.getString(R.string.error_get_friend_info);
                            mMainView.showError(error, message);
                        });
            }
        };
    }

    public void addFriend() {
        mRedditService.addFriend(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> mMainView.showSpinner())
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(
                        response -> {
                            mSummaryView.setFriendButtonState(true);
                            UserIdentity self = mIdentityManager.getUserIdentity();
                            if (self != null && self.isGold()) {
                                mSummaryView.showFriendNote("");
                            }
                            mMainView.showToast(mContext.getString(R.string.user_friend_add_confirm));
                        },
                        error -> {
                            Timber.w(error, "Error adding friend");
                            String message = mContext.getString(R.string.user_friend_add_error);
                            mMainView.showError(error, message);
                        }
                );
    }

    public void deleteFriend() {
        mRedditService.deleteFriend(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(mMainView::showSpinner)
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(
                        response -> {
                            mSummaryView.setFriendButtonState(false);
                            mSummaryView.hideFriendNote();
                            mMainView.showToast(mContext.getString(R.string.user_friend_delete_confirm));
                        },
                        error -> {
                            Timber.w(error, "Error deleting friend");
                            String message = mContext.getString(R.string.user_friend_delete_error);
                            mMainView.showError(error, message);
                        }
                );
    }

    public void saveFriendNote(@NonNull String note) {
        // Note must be non-empty for a positive response
        if (TextUtils.isEmpty(note))
            mMainView.showToast(mContext.getString(R.string.user_friend_empty_note));
        else {
            mMainView.showSpinner();
            mRedditService.saveFriendNote(mSummaryView.getUsernameContext(), note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnTerminate(mMainView::dismissSpinner)
                    .subscribe(
                            result -> {
                                String message = mContext.getString(R.string.user_friend_note_save_confirm);
                                mMainView.showToast(message);
                            },
                            error -> {
                                Timber.w(error, "Error saving friend note");
                                String message = mContext.getString(R.string.user_friend_note_save_error);
                                mMainView.showError(error, message);
                            }
                    );
        }
    }
}
