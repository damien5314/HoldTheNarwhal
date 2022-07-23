package com.ddiehl.android.htn.listings.profile;

import android.text.TextUtils;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.comments.AddCommentDialogRouter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.listings.report.ReportViewRouter;
import com.ddiehl.android.htn.routing.AppRouter;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
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

    private final MainView mainView;
    private final UserProfileView summaryView;

    @Inject
    public UserProfilePresenter(
            MainView main,
            AppRouter appRouter,
            LinkCommentsRouter linkCommentsRouter,
            MediaGalleryRouter mediaGalleryRouter,
            VideoPlayerRouter videoPlayerRouter,
            AddCommentDialogRouter addCommentDialogRouter,
            ReportViewRouter reportViewRouter,
            UserProfileView view) {
        super(
                main,
                appRouter,
                linkCommentsRouter,
                mediaGalleryRouter,
                videoPlayerRouter,
                addCommentDialogRouter,
                reportViewRouter,
                view
        );
        mainView = main;
        summaryView = view;
    }

    public boolean isAuthenticatedUser() {
        UserIdentity authenticatedUser = identityManager.getUserIdentity();
        return authenticatedUser != null
                && Utils.equals(summaryView.getUsernameContext(), authenticatedUser.getName());
    }

    @Override
    protected void requestPreviousData() {
        if ("summary".equals(summaryView.getShow())) {
            getSummaryData();
        } else {
            getListingData(false);
        }
    }

    @Override
    public void requestNextData() {
        if ("summary".equals(summaryView.getShow())) {
            getSummaryData();
        } else {
            getListingData(true);
        }
    }

    private void getListingData(boolean append) {
        String before = append ? null : prevPageListingId;
        String after = append ? nextPageListingId : null;
        redditService.loadUserProfile(
                        summaryView.getShow(), summaryView.getUsernameContext(),
                        summaryView.getSort(), summaryView.getTimespan(),
                        before, after
                )
                .flatMap(this::checkNullResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    mainView.showSpinner();
                    if (append) nextRequested = true;
                    else beforeRequested = true;
                })
                .doFinally(() -> {
                    mainView.dismissSpinner();
                    if (append) nextRequested = false;
                    else beforeRequested = false;
                })
                .subscribe(
                        listings -> onListingsLoaded(listings, append),
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error loading profile listings");
                                String message = context.getString(R.string.error_get_user_profile_listings);
                                mainView.showError(message);
                            }
                        }
                );
    }

    public void requestData() {
        refreshData();
    }

    Observable<UserInfoTuple> getUserInfo() {
        return redditService.getUserInfo(summaryView.getUsernameContext())
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
        return redditService.getUserTrophies(summaryView.getUsernameContext())
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
                    mainView.showSpinner();
                    nextRequested = true;
                })
                .doFinally(() -> {
                    mainView.dismissSpinner();
                    nextRequested = false;
                })
                .subscribe(this::onGetUserInfo, this::onGetUserInfoError);
    }

    void onGetUserInfo(UserInfoTuple info) {
        Timber.i("Showing user profile summary: %s", info.user.getId());

        // Show user info and trophies
        summaryView.showUserInfo(info.user);
        summaryView.showTrophies(info.trophies);

        // Show friend note if we received it, and user is gold
        if (info.user != null && info.user.isGold() && info.friend != null) {
            summaryView.showFriendNote(info.friend.getNote());
        }
    }

    void onGetUserInfoError(Throwable error) {
        if (error instanceof IOException) {
            String message = context.getString(R.string.error_network_unavailable);
            mainView.showError(message);
        } else {
            Timber.w(error, "Error loading friend info");
            String message = context.getString(R.string.error_get_user_info);
            mainView.showError(message);
        }
    }

    private Function<UserInfoTuple, Observable<UserInfoTuple>> getFriendInfo() {
        return (tuple) -> {
            if (tuple.user.isFriend()) {
                return redditService.getFriendInfo(tuple.user.getName())
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
        redditService.addFriend(summaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mainView.showSpinner())
                .doOnTerminate(mainView::dismissSpinner)
                .subscribe(
                        () -> {
                            summaryView.setFriendButtonState(true);
                            UserIdentity self = identityManager.getUserIdentity();
                            if (self != null && self.isGold()) {
                                summaryView.showFriendNote("");
                            }
                            mainView.showToast(context.getString(R.string.user_friend_add_confirm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error adding friend");
                                String message = context.getString(R.string.user_friend_add_error);
                                mainView.showError(message);
                            }
                        }
                );
    }

    public void deleteFriend() {
        redditService.deleteFriend(summaryView.getUsernameContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mainView.showSpinner())
                .doOnTerminate(mainView::dismissSpinner)
                .subscribe(
                        () -> {
                            summaryView.setFriendButtonState(false);
                            summaryView.hideFriendNote();
                            mainView.showToast(context.getString(R.string.user_friend_delete_confirm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error deleting friend");
                                String message = context.getString(R.string.user_friend_delete_error);
                                mainView.showError(message);
                            }
                        }
                );
    }

    public void saveFriendNote(@NotNull String note) {
        // Note must be non-empty for a positive response
        if (TextUtils.isEmpty(note))
            mainView.showToast(context.getString(R.string.user_friend_empty_note));
        else {
            mainView.showSpinner();
            redditService.saveFriendNote(summaryView.getUsernameContext(), note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnTerminate(mainView::dismissSpinner)
                    .subscribe(
                            () -> {
                                String message = context.getString(R.string.user_friend_note_save_confirm);
                                mainView.showToast(message);
                            },
                            error -> {
                                if (error instanceof IOException) {
                                    String message = context.getString(R.string.error_network_unavailable);
                                    mainView.showError(message);
                                } else {
                                    Timber.w(error, "Error saving friend note");
                                    String message = context.getString(R.string.user_friend_note_save_error);
                                    mainView.showError(message);
                                }
                            }
                    );
        }
    }
}
