package com.ddiehl.android.htn.listings.profile;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.MainView;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import rxreddit.model.FriendInfo;
import rxreddit.model.Listing;
import rxreddit.model.UserIdentity;
import timber.log.Timber;

public class UserProfilePresenter extends BaseListingsPresenter {

    static class UserInfoTuple {
        public UserIdentity user;
        public FriendInfo friend;
        public List<Listing> trophies;
    }

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
    protected void requestPreviousData() {
        if ("summary".equals(mSummaryView.getShow())) {
            getSummaryData();
        } else {
            getListingData(false);
        }
    }

    @Override
    public void requestNextData() {
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
                before, after
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
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
                        listings -> onListingsLoaded(listings, append),
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error loading profile listings");
                                String message = mContext.getString(R.string.error_get_user_profile_listings);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    public void requestData() {
        refreshData();
    }

    Observable<UserInfoTuple> getUserInfo() {
        return mRedditService.getUserInfo(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(identity -> {
                    UserInfoTuple info = new UserInfoTuple();
                    info.user = identity;
                    return info;
                })
                .flatMap(getFriendInfo());
    }

    Observable<List<Listing>> getTrophies() {
        return mRedditService.getUserTrophies(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void getSummaryData() {
        Observable.combineLatest(
                getUserInfo(), getTrophies(),
                // Combine trophies into the user info tuple
                (tuple, trophies) -> {
                    tuple.trophies = trophies;
                    return tuple;
                }
        )
                .doOnSubscribe(disposable -> {
                    mMainView.showSpinner();
                    mNextRequested = true;
                })
                .doFinally(() -> {
                    mMainView.dismissSpinner();
                    mNextRequested = false;
                })
                .subscribe(this::onGetUserInfo, this::onGetUserInfoError);
    }

    void onGetUserInfo(UserInfoTuple info) {
        Timber.i("Showing user profile summary: %s", info.user.getId());

        // Show user info and trophies
        mSummaryView.showUserInfo(info.user);
        mSummaryView.showTrophies(info.trophies);

        // Show friend note if we received it, and user is gold
        if (info.user != null && info.user.isGold() && info.friend != null) {
            mSummaryView.showFriendNote(info.friend.getNote());
        }
    }

    void onGetUserInfoError(Throwable error) {
        Timber.d("[DCD] If this is called...");
        if (error instanceof IOException) {
            String message = mContext.getString(R.string.error_network_unavailable);
            mMainView.showError(message);
        } else {
            Timber.w(error, "Error loading friend info");
            String message = mContext.getString(R.string.error_get_user_info);
            mMainView.showError(message);
        }
    }

    private Function<UserInfoTuple, Observable<UserInfoTuple>> getFriendInfo() {
        return (tuple) -> {
            if (tuple.user.isFriend()) {
                return mRedditService.getFriendInfo(tuple.user.getName())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(friendInfo -> {
                            tuple.friend = friendInfo;
                            return tuple;
                        });
            } else {
                return Observable.just(tuple);
            }
        };
    }

    public void addFriend() {
        mRedditService.addFriend(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mMainView.showSpinner())
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(
                        () -> {
                            mSummaryView.setFriendButtonState(true);
                            UserIdentity self = mIdentityManager.getUserIdentity();
                            if (self != null && self.isGold()) {
                                mSummaryView.showFriendNote("");
                            }
                            mMainView.showToast(mContext.getString(R.string.user_friend_add_confirm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error adding friend");
                                String message = mContext.getString(R.string.user_friend_add_error);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    public void deleteFriend() {
        mRedditService.deleteFriend(mSummaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mMainView.showSpinner())
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(
                        () -> {
                            mSummaryView.setFriendButtonState(false);
                            mSummaryView.hideFriendNote();
                            mMainView.showToast(mContext.getString(R.string.user_friend_delete_confirm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error deleting friend");
                                String message = mContext.getString(R.string.user_friend_delete_error);
                                mMainView.showError(message);
                            }
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
                            () -> {
                                String message = mContext.getString(R.string.user_friend_note_save_confirm);
                                mMainView.showToast(message);
                            },
                            error -> {
                                if (error instanceof IOException) {
                                    String message = mContext.getString(R.string.error_network_unavailable);
                                    mMainView.showError(message);
                                } else {
                                    Timber.w(error, "Error saving friend note");
                                    String message = mContext.getString(R.string.user_friend_note_save_error);
                                    mMainView.showError(message);
                                }
                            }
                    );
        }
    }
}
