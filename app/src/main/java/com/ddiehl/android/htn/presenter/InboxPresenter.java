package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.InboxView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
    void requestPreviousData() {
        requestData(false);
    }

    @Override
    void requestNextData() {
        requestData(true);
    }

    private void requestData(boolean append) {
        // TODO Analytics
        String prevId = append ? null : mPrevPageListingId;
        String nextId = append ? mNextPageListingId : null;
        mRedditService.getInbox(mInboxView.getShow(), prevId, nextId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> {
                    mMainView.showSpinner();
                    mNextRequested = true;
                })
                .doOnTerminate(() -> {
                    mMainView.dismissSpinner();
                    mNextRequested = false;
                })
                .subscribe(
                        onListingsLoaded(append),
                        error -> {
                            Timber.w(error, "Error retrieving inbox listings");
                            String message = mContext.getString(R.string.error_get_inbox);
                            mMainView.showError(message);
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
                        result -> {
                            for (Listing listing : mListings) {
                                if (listing instanceof PrivateMessage) {
                                    ((PrivateMessage) listing).markUnread(false);
                                }
                            }
                            mInboxView.notifyDataSetChanged();
                        },
                        error -> {
                            Timber.w(error, "Error marking messages read");
                            String message = mContext.getString(R.string.error_xxx);
                            mMainView.showError(message);
                        }
                );
    }

    public void onViewSelected(String show) {
        refreshData();
    }
}
