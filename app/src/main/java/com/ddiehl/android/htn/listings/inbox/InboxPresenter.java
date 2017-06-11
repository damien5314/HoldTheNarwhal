package com.ddiehl.android.htn.listings.inbox;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.view.MainView;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;
import timber.log.Timber;

public class InboxPresenter extends BaseListingsPresenter {

    private final InboxView mInboxView;

    public InboxPresenter(MainView main, RedditNavigationView navigationView, InboxView inbox) {
        super(main, navigationView, inbox, inbox, inbox, inbox);
        mInboxView = inbox;
    }

    @Override
    protected void requestPreviousData() {
        requestData(false);
    }

    @Override
    protected void requestNextData() {
        requestData(true);
    }

    private void requestData(boolean append) {
        String show = mInboxView.getShow();
        Timber.i("Getting inbox (%s)", show);

        String prevId = append ? null : mPrevPageListingId;
        String nextId = append ? mNextPageListingId : null;

        mRedditService.getInbox(show, prevId, nextId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    mMainView.showSpinner();
                    mNextRequested = true;
                })
                .doFinally(() -> {
                    mMainView.dismissSpinner();
                    mNextRequested = false;
                })
                .subscribe(
                        listings -> onListingsLoaded(listings, append),
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error retrieving inbox listings");
                                String message = mContext.getString(R.string.error_get_inbox);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    public void requestData() {
        refreshData();
    }

    public void onMarkMessagesRead() {
        mRedditService.markAllMessagesRead()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            for (Listing listing : getListings()) {
                                if (listing instanceof PrivateMessage) {
                                    ((PrivateMessage) listing).markUnread(false);
                                }
                            }
                            mInboxView.notifyDataSetChanged();
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error marking messages read");
                                String message = mContext.getString(R.string.error_xxx);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    public void onViewSelected(String show) {
        refreshData();
    }
}
